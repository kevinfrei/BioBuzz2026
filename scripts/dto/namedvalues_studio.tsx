import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { atom } from 'jotai';

import {
  AlertTriangle,
  ArrowRight,
  Code,
  Compass,
  Copy,
  Eye,
  FileDown,
  FileUp,
  Layers,
  Moon,
  Move,
  Plus,
  RefreshCw,
  Search,
  Sparkles,
  Sun,
  Trash2,
  ZoomIn,
  ZoomOut,
} from 'lucide-react';
import { hasField, isDefined } from '@freik/typechk';

import {
  chkConstInterp,
  chkFacePtInterp,
  chkLinearInterp,
  chkPieceWiseInterp,
  chkRef,
  chkTangentInterp,
  InterpPiece,
  InterpRef,
  NamedValues,
  PoseRef,
  ResolvedInterpolator,
  ResolvedPose,
  ResolvedValue,
  SymbolTable,
  ValRef,
} from './dto_schema';

const SAMPLE_AUTONOMOUS_PRESET: NamedValues = {
  values: {
    startX: { val: -5.0 },
    startY: { val: -2.0 },
    startHeading: { val: 0.0 },
    targetX: { val: 4.5 },
    targetY: { val: 3.2 },
    targetHeading: { val: 90.0 },
    midpointX: { val: 0.0 },
    midpointY: { val: 1.5 },
    cutoffStep: { val: 0.5 },
  },
  poses: {
    startPose: {
      X: { ref: 'startX' },
      Y: { ref: 'startY' },
      Heading: { ref: 'startHeading' },
      inRadians: false,
    },
    waypointPose: {
      X: { ref: 'midpointX' },
      Y: { ref: 'midpointY' },
      Heading: { val: 45.0 },
      inRadians: false,
    },
    targetPose: {
      X: { ref: 'targetX' },
      Y: { ref: 'targetY' },
      Heading: { ref: 'targetHeading' },
      inRadians: false,
    },
  },
  interpolations: {
    tangentInterp: { reversed: false },
    linearHeadingInterp: {
      startHeading: { ref: 'startHeading' },
      endHeading: { ref: 'targetHeading' },
      longWay: false,
    },
    faceTargetInterp: {
      point: { ref: 'targetPose' },
    },
    piecewiseInterp: {
      pieces: [
        {
          until: { ref: 'cutoffStep' },
          interpolator: { ref: 'tangentInterp' },
        },
        {
          until: { val: 1.0 },
          interpolator: { ref: 'linearHeadingInterp' },
        },
      ],
    },
  },
  curves: {
    approachCurve: {
      points: [{ ref: 'startPose' }, { ref: 'waypointPose' }],
      interpolation: { ref: 'tangentInterp' },
    },
    finishCurve: {
      points: [{ ref: 'waypointPose' }, { ref: 'targetPose' }],
      interpolation: { ref: 'linearHeadingInterp' },
    },
  },
  paths: {
    mainAutoPath: {
      curves: [{ ref: 'approachCurve' }, { ref: 'finishCurve' }],
      globalInterpolator: { ref: 'tangentInterp' },
    },
  },
};

const EMPTY_WORKSPACE_PRESET: NamedValues = {
  values: {},
  poses: {},
  interpolations: {},
  curves: {},
  paths: {},
};

const namedValuesAtom = atom<NamedValues>(SAMPLE_AUTONOMOUS_PRESET);
const activeTabAtom = atom<
  | 'values'
  | 'poses'
  | 'interpolations'
  | 'curves'
  | 'paths'
  | 'visualizer'
  | 'json'
>('values');
const selectedKeyAtom = atom({ store: 'values', key: 'startX' });
const searchFilterAtom = atom('');
const themeAtom = atom<'dark' | 'light'>('dark');
const visualizerSettingsAtom = atom({
  showGrid: true,
  showLabels: true,
  showVectors: true,
  gridStep: 1.0,
  pathResolution: 30,
});
const toastAtom = atom(null);

function resolveValRef(
  valRef: ValRef,
  dict: SymbolTable,
  seen = new Set<string>(),
): ResolvedValue {
  if (chkRef(valRef)) {
    if (seen.has(valRef.ref)) {
      return { err: `Circular value reference detected :${valRef.ref}` };
    }
    seen.add(valRef.ref);
    const lkup = dict.values.get(valRef.ref);
    return isDefined(lkup)
      ? resolveValRef(lkup, dict, seen)
      : { err: `Missing value reference: ${valRef.ref}` };
  }
  // The ValRef's val is the resolved value
  return valRef.val;
}

function resolvePoseRef(
  poseRef: PoseRef,
  dict: SymbolTable,
  seen = new Set<string>(),
): ResolvedPose {
  if (chkRef(poseRef)) {
    if (seen.has(poseRef.ref)) {
      return { err: `Circular pose reference detected :${poseRef.ref}` };
    }
    seen.add(poseRef.ref);
    const lkup = dict.poses.get(poseRef.ref);
    return isDefined(lkup)
      ? resolvePoseRef(lkup, dict, seen)
      : { err: `Missing pose reference: ${poseRef.ref}` };
  }
  // Resolve the PoseRef's individual components:
  const X = resolveValRef(poseRef.X, dict);
  const Y = resolveValRef(poseRef.Y, dict);
  if (hasField(poseRef, 'Heading') && isDefined(poseRef.Heading)) {
    let Heading = resolveValRef(poseRef.Heading, dict);
    if (hasField(Heading, 'err')) {
      return { X, Y, Heading };
    }
    if (!poseRef.inRadians) {
      Heading = (Math.PI * Heading) / 180.0;
    }
    return { X, Y, Heading };
  } else {
    return { X, Y };
  }
}

function resolveInterpRef(
  interpRef: InterpRef,
  dict: SymbolTable,
  seen = new Set<string>(),
): ResolvedInterpolator {
  if (chkRef(interpRef)) {
    if (seen.has(interpRef.ref)) {
      return {
        err: `Circular interpolator reference detected :${interpRef.ref}`,
      };
    }
    seen.add(interpRef.ref);
    const lkup = dict.interpolations.get(interpRef.ref);
    return isDefined(lkup)
      ? resolveInterpRef(lkup, dict, seen)
      : { err: `Missing interpolator reference: ${interpRef.ref}` };
  }
  // Resolve the Interpolator, since it's not a reference
  if (chkTangentInterp(interpRef)) {
    // Tangent's are dumb: it's literally just
    // "yup, it's tangent: Maybe it's reversed?"
    return interpRef;
  } else if (chkConstInterp(interpRef)) {
    return { heading: resolveValRef(interpRef.heading, dict) };
  } else if (chkFacePtInterp(interpRef)) {
    return { point: resolvePoseRef(interpRef.point, dict) };
  } else if (chkLinearInterp(interpRef)) {
    return {
      startHeading: resolveValRef(interpRef.startHeading, dict),
      endHeading: resolveValRef(interpRef.endHeading, dict),
      longWay: interpRef.longWay,
    };
  } else if (chkPieceWiseInterp(interpRef)) {
    return {
      pieces: interpRef.pieces.map((piece: InterpPiece) => ({
        until: resolveValRef(piece.until, dict),
        interpolater: resolveInterpRef(piece.interpolator, dict, seen),
      })),
    };
  }
  return { err: `Unknown interpolator type ${interpRef}` };
}

function resolveCurveRef(curveRef, namedValues, depth = 0) {
  if (!curveRef || depth > 10) return null;
  if ('ref' in curveRef) {
    const key = curveRef.ref;
    const target = namedValues.curves?.[key];
    if (!target) return { refKey: key, missing: true, points: [] };
    const res = resolveCurveRef(target, namedValues, depth + 1);
    return res ? { ...res, refKey: key } : null;
  }

  const points = (curveRef.points || []).map((pRef) =>
    resolvePoseRef(pRef, namedValues),
  );
  const interpolation = resolveInterpRef(curveRef.interpolation, namedValues);

  return {
    points,
    interpolation,
    raw: curveRef,
  };
}

function resolvePath(path, namedValues) {
  if (!path || !path.curves) return [];
  return path.curves
    .map((cRef) => resolveCurveRef(cRef, namedValues))
    .filter(Boolean);
}

function NotificationToast() {
  const [toast, setToast] = useAtom(toastAtom);
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast, setToast]);

  if (!toast) return null;

  return (
    <div className="fixed bottom-5 right-5 z-50 flex items-center gap-3 px-4 py-3 rounded-md bg-neutral-900 text-white shadow-xl border border-neutral-700 animate-bounce-short">
      <Sparkles className="w-4 h-4 text-sky-400" />
      <span className="text-sm font-medium">{toast}</span>
    </div>
  );
}

// ValRef Control: Switch between Inline ({ val }) and Ref ({ ref })
function ValRefControl({ label, value, onChange, availableKeys = [] }) {
  const [namedValues] = useAtom(namedValuesAtom);
  const isRef = Boolean(value && 'ref' in value);
  const resolved = resolveValRef(value, namedValues.values || {});

  const toggleType = (toRef) => {
    if (toRef) {
      const firstKey = availableKeys[0] || '';
      onChange({ ref: firstKey });
    } else {
      onChange({ val: resolved.isValid ? resolved.val : 0 });
    }
  };

  return (
    <div className="p-3 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/60 transition-all">
      <div className="flex items-center justify-between mb-2">
        <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider">
          {label}
        </label>
        <div className="flex items-center bg-neutral-200 dark:bg-neutral-800 p-0.5 rounded-md text-xs">
          <button
            type="button"
            onClick={() => toggleType(false)}
            className={`px-2 py-0.5 rounded ${
              !isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Inline Value
          </button>
          <button
            type="button"
            onClick={() => toggleType(true)}
            className={`px-2 py-0.5 rounded ${
              isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Reference
          </button>
        </div>
      </div>

      {!isRef ? (
        <div className="flex items-center gap-2">
          <input
            type="number"
            step="any"
            value={value && 'val' in value ? value.val : 0}
            onChange={(e) => onChange({ val: parseFloat(e.target.value) || 0 })}
            className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none"
          />
        </div>
      ) : (
        <div className="space-y-2">
          <select
            value={value?.ref || ''}
            onChange={(e) => onChange({ ref: e.target.value })}
            className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none">
            <option value="" disabled>
              Select Value Reference...
            </option>
            {availableKeys.map((k) => (
              <option key={k} value={k}>
                {k} (val: {namedValues.values?.[k]?.val})
              </option>
            ))}
          </select>
          {resolved.missing && (
            <div className="flex items-center gap-1.5 text-xs text-rose-500 dark:text-rose-400">
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Missing reference: "{value?.ref}"</span>
            </div>
          )}
        </div>
      )}

      {/* Resolved summary badge */}
      <div className="mt-2 text-right">
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-mono ${
            resolved.isValid
              ? 'bg-sky-100 dark:bg-sky-950/60 text-sky-700 dark:text-sky-300 border border-sky-200 dark:border-sky-800/50'
              : 'bg-rose-100 dark:bg-rose-950/60 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-800/50'
          }`}>
          <span>Resolved:</span>
          <strong className="font-bold">
            {resolved.isValid ? resolved.val : 'NaN'}
          </strong>
        </span>
      </div>
    </div>
  );
}

// PoseRef Control: Switch between Inline ({ X, Y, Heading, inRadians }) and Ref ({ ref })
function PoseRefControl({ label, value, onChange }) {
  const [namedValues] = useAtom(namedValuesAtom);
  const poseKeys = Object.keys(namedValues.poses || {});
  const valueKeys = Object.keys(namedValues.values || {});

  const isRef = Boolean(value && 'ref' in value);
  const resolved = resolvePoseRef(value, namedValues);

  const toggleType = (toRef) => {
    if (toRef) {
      onChange({ ref: poseKeys[0] || '' });
    } else {
      onChange({
        X: { val: 0 },
        Y: { val: 0 },
        Heading: { val: 0 },
        inRadians: false,
      });
    }
  };

  return (
    <div className="p-3.5 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/60 space-y-3">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider">
          {label}
        </span>
        <div className="flex items-center bg-neutral-200 dark:bg-neutral-800 p-0.5 rounded-md text-xs">
          <button
            type="button"
            onClick={() => toggleType(false)}
            className={`px-2 py-0.5 rounded ${
              !isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Inline Pose
          </button>
          <button
            type="button"
            onClick={() => toggleType(true)}
            className={`px-2 py-0.5 rounded ${
              isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Reference
          </button>
        </div>
      </div>

      {isRef ? (
        <div className="space-y-2">
          <select
            value={value?.ref || ''}
            onChange={(e) => onChange({ ref: e.target.value })}
            className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none">
            <option value="" disabled>
              Select Pose Reference...
            </option>
            {poseKeys.map((k) => (
              <option key={k} value={k}>
                {k}
              </option>
            ))}
          </select>
          {resolved.missing && (
            <div className="flex items-center gap-1.5 text-xs text-rose-500 dark:text-rose-400">
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Missing pose reference: "{value?.ref}"</span>
            </div>
          )}
        </div>
      ) : (
        <div className="space-y-3 pl-1 border-l-2 border-sky-500/30">
          <ValRefControl
            label="X Coordinate"
            value={value?.X || { val: 0 }}
            onChange={(X) => onChange({ ...value, X })}
            availableKeys={valueKeys}
          />
          <ValRefControl
            label="Y Coordinate"
            value={value?.Y || { val: 0 }}
            onChange={(Y) => onChange({ ...value, Y })}
            availableKeys={valueKeys}
          />
          <ValRefControl
            label="Heading Angle"
            value={value?.Heading || { val: 0 }}
            onChange={(Heading) => onChange({ ...value, Heading })}
            availableKeys={valueKeys}
          />

          <div className="flex items-center justify-between pt-1">
            <span className="text-xs text-neutral-600 dark:text-neutral-400">
              Angle Unit
            </span>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={Boolean(value?.inRadians)}
                onChange={(e) =>
                  onChange({ ...value, inRadians: e.target.checked })
                }
                className="sr-only peer"
              />
              <div className="w-9 h-5 bg-neutral-300 peer-focus:outline-none rounded-full peer dark:bg-neutral-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:peer-checked:after:border-neutral-700 peer-checked:bg-sky-600"></div>
              <span className="ml-2 text-xs font-mono font-medium text-neutral-800 dark:text-neutral-200">
                {value?.inRadians ? 'Radians (rad)' : 'Degrees (°)'}
              </span>
            </label>
          </div>
        </div>
      )}

      {/* Resolved Position Badge */}
      <div className="flex items-center justify-between pt-1 text-xs font-mono text-neutral-600 dark:text-neutral-400 border-t border-neutral-200 dark:border-neutral-800">
        <span>Computed Position:</span>
        <span className="font-semibold text-neutral-900 dark:text-neutral-100">
          X: {resolved.x.toFixed(2)}, Y: {resolved.y.toFixed(2)}, θ:{' '}
          {resolved.headingDeg.toFixed(1)}°
        </span>
      </div>
    </div>
  );
}

function InterpRefControl({ label, value, onChange }) {
  const [namedValues] = useAtom(namedValuesAtom);
  const interpKeys = Object.keys(namedValues.interpolations || {});
  const valueKeys = Object.keys(namedValues.values || {});

  const currentType = getInterpolatorType(value);

  const handleTypeChange = (newType) => {
    switch (newType) {
      case 'Reference':
        onChange({ ref: interpKeys[0] || '' });
        break;
      case 'ConstInterp':
        onChange({ heading: { val: 0 } });
        break;
      case 'FacePtInterp':
        onChange({
          point: {
            X: { val: 0 },
            Y: { val: 0 },
            Heading: { val: 0 },
            inRadians: false,
          },
        });
        break;
      case 'LinearInterp':
        onChange({
          startHeading: { val: 0 },
          endHeading: { val: 180 },
          longWay: false,
        });
        break;
      case 'TangentInterp':
        onChange({ reversed: false });
        break;
      case 'PieceWiseInterp':
        onChange({
          pieces: [
            {
              until: { val: 0.5 },
              interpolator: { reversed: false },
            },
          ],
        });
        break;
      default:
        break;
    }
  };

  return (
    <div className="p-3.5 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/60 space-y-3">
      <div className="flex items-center justify-between">
        <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider">
          {label}
        </label>
        <select
          value={currentType}
          onChange={(e) => handleTypeChange(e.target.value)}
          className="px-2.5 py-1 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-sky-600 dark:text-sky-400 font-semibold outline-none focus:ring-2 focus:ring-sky-500">
          <option value="ConstInterp">ConstInterp (Fixed Heading)</option>
          <option value="FacePtInterp">FacePtInterp (Point at Pose)</option>
          <option value="LinearInterp">LinearInterp (Heading Range)</option>
          <option value="TangentInterp">TangentInterp (Along Path)</option>
          <option value="PieceWiseInterp">
            PieceWiseInterp (Multi-Segment)
          </option>
          <option value="Reference">Reference (from Interpolations)</option>
        </select>
      </div>

      {currentType === 'Reference' && (
        <div className="space-y-2">
          <select
            value={value?.ref || ''}
            onChange={(e) => onChange({ ref: e.target.value })}
            className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none">
            <option value="" disabled>
              Select Interpolator Reference...
            </option>
            {interpKeys.map((k) => (
              <option key={k} value={k}>
                {k} ({getInterpolatorType(namedValues.interpolations?.[k])})
              </option>
            ))}
          </select>
        </div>
      )}

      {currentType === 'ConstInterp' && (
        <ValRefControl
          label="Heading Value"
          value={value?.heading || { val: 0 }}
          onChange={(heading) => onChange({ heading })}
          availableKeys={valueKeys}
        />
      )}

      {currentType === 'FacePtInterp' && (
        <PoseRefControl
          label="Target Point Pose"
          value={
            value?.point || {
              X: { val: 0 },
              Y: { val: 0 },
              Heading: { val: 0 },
              inRadians: false,
            }
          }
          onChange={(point) => onChange({ point })}
        />
      )}

      {currentType === 'LinearInterp' && (
        <div className="space-y-3">
          <ValRefControl
            label="Start Heading"
            value={value?.startHeading || { val: 0 }}
            onChange={(startHeading) => onChange({ ...value, startHeading })}
            availableKeys={valueKeys}
          />
          <ValRefControl
            label="End Heading"
            value={value?.endHeading || { val: 180 }}
            onChange={(endHeading) => onChange({ ...value, endHeading })}
            availableKeys={valueKeys}
          />
          <div className="flex items-center justify-between">
            <span className="text-xs text-neutral-600 dark:text-neutral-400">
              Take Long Way Arc
            </span>
            <input
              type="checkbox"
              checked={Boolean(value?.longWay)}
              onChange={(e) =>
                onChange({ ...value, longWay: e.target.checked })
              }
              className="w-4 h-4 text-sky-600 rounded border-neutral-300 focus:ring-sky-500"
            />
          </div>
        </div>
      )}

      {currentType === 'TangentInterp' && (
        <div className="flex items-center justify-between py-1">
          <span className="text-xs text-neutral-600 dark:text-neutral-400">
            Reversed Path Heading
          </span>
          <input
            type="checkbox"
            checked={Boolean(value?.reversed)}
            onChange={(e) => onChange({ reversed: e.target.checked })}
            className="w-4 h-4 text-sky-600 rounded border-neutral-300 focus:ring-sky-500"
          />
        </div>
      )}

      {currentType === 'PieceWiseInterp' && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-neutral-600 dark:text-neutral-400">
              Piecewise Interpolator Segments
            </span>
            <button
              type="button"
              onClick={() => {
                const pieces = value?.pieces || [];
                onChange({
                  pieces: [
                    ...pieces,
                    { until: { val: 1.0 }, interpolator: { reversed: false } },
                  ],
                });
              }}
              className="px-2 py-1 text-xs rounded bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 font-medium">
              <Plus className="w-3 h-3" /> Add Piece
            </button>
          </div>

          {(value?.pieces || []).map((piece, idx) => (
            <div
              key={idx}
              className="p-3 rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 space-y-3 relative group">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-sky-600 dark:text-sky-400">
                  Piece #{idx + 1}
                </span>
                <button
                  type="button"
                  onClick={() => {
                    const newPieces = value.pieces.filter((_, i) => i !== idx);
                    onChange({ pieces: newPieces });
                  }}
                  className="text-neutral-400 hover:text-rose-500 transition-colors"
                  title="Remove Piece">
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>

              <ValRefControl
                label="Until Param (t)"
                value={piece.until}
                onChange={(until) => {
                  const newPieces = [...value.pieces];
                  newPieces[idx] = { ...newPieces[idx], until };
                  onChange({ pieces: newPieces });
                }}
                availableKeys={valueKeys}
              />

              <InterpRefControl
                label="Segment Interpolator"
                value={piece.interpolator}
                onChange={(interpolator) => {
                  const newPieces = [...value.pieces];
                  newPieces[idx] = { ...newPieces[idx], interpolator };
                  onChange({ pieces: newPieces });
                }}
              />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// CurveRef Control: Inline Curve vs Ref ({ ref })
function CurveRefControl({ label, value, onChange }) {
  const [namedValues] = useAtom(namedValuesAtom);
  const curveKeys = Object.keys(namedValues.curves || {});
  const isRef = Boolean(value && 'ref' in value);

  const toggleType = (toRef) => {
    if (toRef) {
      onChange({ ref: curveKeys[0] || '' });
    } else {
      onChange({
        points: [
          {
            X: { val: 0 },
            Y: { val: 0 },
            Heading: { val: 0 },
            inRadians: false,
          },
        ],
        interpolation: { reversed: false },
      });
    }
  };

  return (
    <div className="p-4 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/60 space-y-3">
      <div className="flex items-center justify-between">
        <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider">
          {label}
        </label>
        <div className="flex items-center bg-neutral-200 dark:bg-neutral-800 p-0.5 rounded-md text-xs">
          <button
            type="button"
            onClick={() => toggleType(false)}
            className={`px-2 py-0.5 rounded ${
              !isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Inline Curve
          </button>
          <button
            type="button"
            onClick={() => toggleType(true)}
            className={`px-2 py-0.5 rounded ${
              isRef
                ? 'bg-white dark:bg-neutral-700 text-sky-600 dark:text-sky-400 font-medium shadow-sm'
                : 'text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-200'
            }`}>
            Reference
          </button>
        </div>
      </div>

      {isRef ? (
        <select
          value={value?.ref || ''}
          onChange={(e) => onChange({ ref: e.target.value })}
          className="w-full px-3 py-1.5 text-sm rounded-md border border-neutral-300 dark:border-neutral-700 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none">
          <option value="" disabled>
            Select Curve Reference...
          </option>
          {curveKeys.map((k) => (
            <option key={k} value={k}>
              {k}
            </option>
          ))}
        </select>
      ) : (
        <div className="space-y-4">
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium text-neutral-700 dark:text-neutral-300">
                Curve Points (Poses)
              </span>
              <button
                type="button"
                onClick={() => {
                  const pts = value?.points || [];
                  onChange({
                    ...value,
                    points: [
                      ...pts,
                      {
                        X: { val: 0 },
                        Y: { val: 0 },
                        Heading: { val: 0 },
                        inRadians: false,
                      },
                    ],
                  });
                }}
                className="px-2 py-0.5 text-xs rounded bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 font-medium">
                <Plus className="w-3 h-3" /> Add Pose
              </button>
            </div>

            {(value?.points || []).map((pt, pIdx) => (
              <div key={pIdx} className="relative pt-1">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-semibold text-neutral-500">
                    Pose #{pIdx + 1}
                  </span>
                  {(value?.points || []).length > 1 && (
                    <button
                      type="button"
                      onClick={() => {
                        const newPts = value.points.filter(
                          (_, i) => i !== pIdx,
                        );
                        onChange({ ...value, points: newPts });
                      }}
                      className="text-neutral-400 hover:text-rose-500 transition-colors"
                      title="Remove Pose Point">
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
                <PoseRefControl
                  label={`Point ${pIdx + 1}`}
                  value={pt}
                  onChange={(newPt) => {
                    const newPts = [...value.points];
                    newPts[pIdx] = newPt;
                    onChange({ ...value, points: newPts });
                  }}
                />
              </div>
            ))}
          </div>

          <InterpRefControl
            label="Curve Interpolator"
            value={value?.interpolation || { reversed: false }}
            onChange={(interpolation) => onChange({ ...value, interpolation })}
          />
        </div>
      )}
    </div>
  );
}

function CanvasVisualizer() {
  const [namedValues] = useAtom(namedValuesAtom);
  const [selectedKey, setSelectedKey] = useAtom(selectedKeyAtom);
  const [settings, setSettings] = useAtom(visualizerSettingsAtom);

  const canvasRef = useRef(null);
  const containerRef = useRef(null);

  // Pan and Zoom state
  const [transform, setTransform] = useState({ panX: 0, panY: 0, zoom: 35 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [hoveredEntity, setHoveredEntity] = useState(null);

  // Reset transform to center grid
  const handleResetView = useCallback(() => {
    if (!containerRef.current) return;
    const { clientWidth, clientHeight } = containerRef.current;
    setTransform({
      panX: clientWidth / 2,
      panY: clientHeight / 2,
      zoom: 35,
    });
  }, []);

  useEffect(() => {
    handleResetView();
  }, [handleResetView]);

  // Center view on resize
  useEffect(() => {
    const handleResize = () => {
      if (containerRef.current && canvasRef.current) {
        canvasRef.current.width = containerRef.current.clientWidth;
        canvasRef.current.height = containerRef.current.clientHeight;
      }
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Compute all resolved Poses, Curves, and Paths
  const resolvedData = useMemo(() => {
    const poses = {};
    Object.keys(namedValues.poses || {}).forEach((k) => {
      poses[k] = resolvePoseRef({ ref: k }, namedValues);
    });

    const curves = {};
    Object.keys(namedValues.curves || {}).forEach((k) => {
      curves[k] = resolveCurveRef({ ref: k }, namedValues);
    });

    const paths = {};
    Object.keys(namedValues.paths || {}).forEach((k) => {
      paths[k] = resolvePath(namedValues.paths[k], namedValues);
    });

    return { poses, curves, paths };
  }, [namedValues]);

  // Draw 2D Cartesian Scene
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;

    ctx.clearRect(0, 0, width, height);

    const { panX, panY, zoom } = transform;

    // Helper functions for coordinate conversion
    const toScreenX = (wx) => panX + wx * zoom;
    const toScreenY = (wy) => panY - wy * zoom; // Invert Y axis (+Y is UP)
    const toWorldX = (sx) => (sx - panX) / zoom;
    const toWorldY = (sy) => (panY - sy) / zoom;

    // 1. Draw Grid
    if (settings.showGrid) {
      ctx.strokeStyle = '#262626';
      ctx.lineWidth = 1;
      const step = settings.gridStep * zoom;

      // Draw vertical lines
      const minX =
        Math.floor(toWorldX(0) / settings.gridStep) * settings.gridStep;
      const maxX =
        Math.ceil(toWorldX(width) / settings.gridStep) * settings.gridStep;

      for (let x = minX; x <= maxX; x += settings.gridStep) {
        const sx = toScreenX(x);
        ctx.beginPath();
        ctx.moveTo(sx, 0);
        ctx.lineTo(sx, height);
        ctx.stroke();
      }

      // Draw horizontal lines
      const minY =
        Math.floor(toWorldY(height) / settings.gridStep) * settings.gridStep;
      const maxY =
        Math.ceil(toWorldY(0) / settings.gridStep) * settings.gridStep;

      for (let y = minY; y <= maxY; y += settings.gridStep) {
        const sy = toScreenY(y);
        ctx.beginPath();
        ctx.moveTo(0, sy);
        ctx.lineTo(width, sy);
        ctx.stroke();
      }

      // Draw Axes (+X Red, +Y Green)
      ctx.lineWidth = 2;
      // X-Axis
      ctx.strokeStyle = '#ef4444';
      ctx.beginPath();
      ctx.moveTo(0, panY);
      ctx.lineTo(width, panY);
      ctx.stroke();

      // Y-Axis
      ctx.strokeStyle = '#22c55e';
      ctx.beginPath();
      ctx.moveTo(panX, 0);
      ctx.lineTo(panX, height);
      ctx.stroke();

      // Origin Marker
      ctx.fillStyle = '#0078d4';
      ctx.beginPath();
      ctx.arc(panX, panY, 4, 0, Math.PI * 2);
      ctx.fill();
    }

    // 2. Draw Curves & Paths
    Object.entries(resolvedData.curves).forEach(([cKey, curveRes]) => {
      if (!curveRes || !curveRes.points || curveRes.points.length < 2) return;

      const isSelected =
        selectedKey.store === 'curves' && selectedKey.key === cKey;
      ctx.lineWidth = isSelected ? 4 : 2.5;
      ctx.strokeStyle = isSelected ? '#38bdf8' : '#0284c7';

      ctx.beginPath();
      curveRes.points.forEach((p, idx) => {
        const sx = toScreenX(p.x);
        const sy = toScreenY(p.y);
        if (idx === 0) ctx.moveTo(sx, sy);
        else ctx.lineTo(sx, sy);
      });
      ctx.stroke();

      // Draw directional ticks along curve
      for (let i = 0; i < curveRes.points.length - 1; i++) {
        const p1 = curveRes.points[i];
        const p2 = curveRes.points[i + 1];
        const mx = toScreenX((p1.x + p2.x) / 2);
        const my = toScreenY((p1.y + p2.y) / 2);
        const angle = Math.atan2(-(p2.y - p1.y), p2.x - p1.x);

        ctx.save();
        ctx.translate(mx, my);
        ctx.rotate(angle);
        ctx.fillStyle = isSelected ? '#38bdf8' : '#0284c7';
        ctx.beginPath();
        ctx.moveTo(6, 0);
        ctx.lineTo(-4, -4);
        ctx.lineTo(-4, 4);
        ctx.closePath();
        ctx.fill();
        ctx.restore();
      }
    });

    // 3. Draw Poses
    Object.entries(resolvedData.poses).forEach(([pKey, pRes]) => {
      if (!pRes.isValid) return;

      const sx = toScreenX(pRes.x);
      const sy = toScreenY(pRes.y);
      const isSelected =
        selectedKey.store === 'poses' && selectedKey.key === pKey;
      const isHovered = hoveredEntity?.key === pKey;

      // Outer Selection Ring
      if (isSelected || isHovered) {
        ctx.beginPath();
        ctx.arc(sx, sy, isSelected ? 14 : 11, 0, Math.PI * 2);
        ctx.strokeStyle = isSelected ? '#38bdf8' : '#a855f7';
        ctx.lineWidth = 2;
        ctx.stroke();
      }

      // Robot Center Circle
      ctx.fillStyle = isSelected ? '#0284c7' : '#0078d4';
      ctx.beginPath();
      ctx.arc(sx, sy, 7, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 1.5;
      ctx.stroke();

      // Heading Arrow Vector
      if (settings.showVectors) {
        const arrowLength = 22;
        const hRad = pRes.headingRad;
        const ex = sx + arrowLength * Math.cos(hRad);
        const ey = sy - arrowLength * Math.sin(hRad); // inverted canvas Y

        ctx.strokeStyle = '#f59e0b';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(sx, sy);
        ctx.lineTo(ex, ey);
        ctx.stroke();

        // Arrow head
        ctx.fillStyle = '#f59e0b';
        ctx.save();
        ctx.translate(ex, ey);
        ctx.rotate(-hRad);
        ctx.beginPath();
        ctx.moveTo(5, 0);
        ctx.lineTo(-3, -3);
        ctx.lineTo(-3, 3);
        ctx.closePath();
        ctx.fill();
        ctx.restore();
      }

      // Pose Label
      if (settings.showLabels) {
        ctx.font = '600 11px Segoe UI, sans-serif';
        ctx.fillStyle = isSelected ? '#38bdf8' : '#e5e5e5';
        ctx.textAlign = 'left';
        ctx.fillText(
          `${pKey} (${pRes.x.toFixed(1)}, ${pRes.y.toFixed(1)})`,
          sx + 12,
          sy - 8,
        );
      }
    });
  }, [transform, settings, resolvedData, selectedKey, hoveredEntity]);

  // Mouse drag handlers for Canvas Panning
  const handleMouseDown = (e) => {
    if (e.button === 0) {
      setIsDragging(true);
      setDragStart({
        x: e.clientX - transform.panX,
        y: e.clientY - transform.panY,
      });
    }
  };

  const handleMouseMove = (e) => {
    if (isDragging) {
      setTransform((prev) => ({
        ...prev,
        panX: e.clientX - dragStart.x,
        panY: e.clientY - dragStart.y,
      }));
      return;
    }

    // Hover detection for Poses
    if (!canvasRef.current) return;
    const rect = canvasRef.current.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    let found = null;
    Object.entries(resolvedData.poses).forEach(([pKey, pRes]) => {
      if (!pRes.isValid) return;
      const sx = transform.panX + pRes.x * transform.zoom;
      const sy = transform.panY - pRes.y * transform.zoom;
      const dist = Math.hypot(mouseX - sx, mouseY - sy);
      if (dist < 12) {
        found = { store: 'poses', key: pKey, res: pRes };
      }
    });

    setHoveredEntity(found);
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Canvas Click Handler: Select Entity
  const handleCanvasClick = () => {
    if (hoveredEntity) {
      setSelectedKey({ store: hoveredEntity.store, key: hoveredEntity.key });
    }
  };

  // Zoom Handler
  const handleWheel = (e) => {
    e.preventDefault();
    const zoomFactor = e.deltaY < 0 ? 1.15 : 0.85;
    setTransform((prev) => ({
      ...prev,
      zoom: Math.max(5, Math.min(200, prev.zoom * zoomFactor)),
    }));
  };

  return (
    <div className="relative w-full h-full min-h-[500px] flex flex-col bg-neutral-950 rounded-xl overflow-hidden border border-neutral-800 shadow-inner">
      {/* Visualizer Control Overlay */}
      <div className="absolute top-4 left-4 z-10 flex items-center gap-2 bg-neutral-900/90 backdrop-blur-md p-2 rounded-lg border border-neutral-800 text-xs shadow-lg">
        <button
          type="button"
          onClick={() =>
            setTransform((prev) => ({ ...prev, zoom: prev.zoom * 1.2 }))
          }
          className="p-1.5 rounded hover:bg-neutral-800 text-neutral-300 hover:text-white"
          title="Zoom In">
          <ZoomIn className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={() =>
            setTransform((prev) => ({ ...prev, zoom: prev.zoom / 1.2 }))
          }
          className="p-1.5 rounded hover:bg-neutral-800 text-neutral-300 hover:text-white"
          title="Zoom Out">
          <ZoomOut className="w-4 h-4" />
        </button>
        <button
          type="button"
          onClick={handleResetView}
          className="p-1.5 rounded hover:bg-neutral-800 text-neutral-300 hover:text-white flex items-center gap-1"
          title="Center View">
          <RefreshCw className="w-3.5 h-3.5" /> Center
        </button>

        <div className="h-4 w-px bg-neutral-800 my-auto" />

        <label className="flex items-center gap-1.5 text-neutral-300 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={settings.showGrid}
            onChange={(e) =>
              setSettings({ ...settings, showGrid: e.target.checked })
            }
            className="rounded text-sky-500 focus:ring-0"
          />
          Grid
        </label>
        <label className="flex items-center gap-1.5 text-neutral-300 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={settings.showVectors}
            onChange={(e) =>
              setSettings({ ...settings, showVectors: e.target.checked })
            }
            className="rounded text-sky-500 focus:ring-0"
          />
          Vectors
        </label>
        <label className="flex items-center gap-1.5 text-neutral-300 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={settings.showLabels}
            onChange={(e) =>
              setSettings({ ...settings, showLabels: e.target.checked })
            }
            className="rounded text-sky-500 focus:ring-0"
          />
          Labels
        </label>
      </div>

      {/* Hover Information Tooltip */}
      {hoveredEntity && (
        <div className="absolute bottom-4 left-4 z-10 bg-sky-950/90 border border-sky-800 text-sky-200 backdrop-blur-md px-3 py-2 rounded-md text-xs font-mono shadow-lg">
          <div className="font-bold text-sky-400">{hoveredEntity.key}</div>
          <div>
            X: {hoveredEntity.res.x.toFixed(2)}, Y:{' '}
            {hoveredEntity.res.y.toFixed(2)}
          </div>
          <div>Heading: {hoveredEntity.res.headingDeg.toFixed(1)}°</div>
        </div>
      )}

      {/* Canvas Viewport */}
      <div
        ref={containerRef}
        className="w-full h-full flex-grow relative cursor-grab active:cursor-grabbing">
        <canvas
          ref={canvasRef}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onClick={handleCanvasClick}
          onWheel={handleWheel}
          className="w-full h-full block"
        />
      </div>
    </div>
  );
}

function JsonViewEditor() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [, setToast] = useAtom(toastAtom);
  const [rawJson, setRawJson] = useState(() =>
    JSON.stringify(namedValues, null, 2),
  );
  const [error, setError] = useState(null);

  useEffect(() => {
    setRawJson(JSON.stringify(namedValues, null, 2));
  }, [namedValues]);

  const handleJsonChange = (val) => {
    setRawJson(val);
    try {
      const parsed = JSON.parse(val);
      setNamedValues(parsed);
      setError(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(rawJson);
    setToast('JSON copied to clipboard!');
  };

  const handleDownload = () => {
    const blob = new Blob([rawJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'named_values.json';
    a.click();
    URL.revokeObjectURL(url);
    setToast('Downloaded named_values.json!');
  };

  const handleFileUpload = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const content = event.target.result;
        const parsed = JSON.parse(content);
        setNamedValues(parsed);
        setRawJson(JSON.stringify(parsed, null, 2));
        setError(null);
        setToast(`Loaded ${file.name} successfully!`);
      } catch (err) {
        setError(`Failed to parse file: ${err.message}`);
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="flex flex-col h-full space-y-3">
      <div className="flex items-center justify-between bg-neutral-100 dark:bg-neutral-800/80 p-3 rounded-lg border border-neutral-200 dark:border-neutral-700">
        <div className="flex items-center gap-2">
          <Code className="w-5 h-5 text-sky-500" />
          <span className="font-semibold text-sm text-neutral-800 dark:text-neutral-200">
            NamedValues JSON Raw Definition
          </span>
        </div>
        <div className="flex items-center gap-2">
          <label className="px-3 py-1.5 text-xs rounded-md bg-white dark:bg-neutral-700 border border-neutral-300 dark:border-neutral-600 hover:bg-neutral-50 dark:hover:bg-neutral-600 cursor-pointer flex items-center gap-1.5 text-neutral-700 dark:text-neutral-200 transition-all font-medium">
            <FileUp className="w-3.5 h-3.5" /> Import File
            <input
              type="file"
              accept=".json"
              onChange={handleFileUpload}
              className="hidden"
            />
          </label>
          <button
            type="button"
            onClick={handleCopy}
            className="px-3 py-1.5 text-xs rounded-md bg-white dark:bg-neutral-700 border border-neutral-300 dark:border-neutral-600 hover:bg-neutral-50 dark:hover:bg-neutral-600 flex items-center gap-1.5 text-neutral-700 dark:text-neutral-200 transition-all font-medium">
            <Copy className="w-3.5 h-3.5" /> Copy
          </button>
          <button
            type="button"
            onClick={handleDownload}
            className="px-3 py-1.5 text-xs rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1.5 transition-all font-medium shadow-sm">
            <FileDown className="w-3.5 h-3.5" /> Download
          </button>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-3 py-2 rounded-md bg-rose-500/10 border border-rose-500/30 text-rose-500 dark:text-rose-400 text-xs font-mono">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>JSON Syntax Error: {error}</span>
        </div>
      )}

      <textarea
        value={rawJson}
        onChange={(e) => handleJsonChange(e.target.value)}
        className="w-full flex-grow font-mono text-xs p-4 rounded-lg border border-neutral-300 dark:border-neutral-800 bg-white dark:bg-neutral-900 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none resize-none leading-relaxed"
        spellCheck={false}
      />
    </div>
  );
}

// Values Store Editor
function ValuesStoreEditor() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [selected, setSelected] = useAtom(selectedKeyAtom);
  const [search, setSearch] = useAtom(searchFilterAtom);
  const [, setToast] = useAtom(toastAtom);

  const values = namedValues.values || {};
  const keys = Object.keys(values).filter((k) =>
    k.toLowerCase().includes(search.toLowerCase()),
  );

  const activeKey = selected.store === 'values' ? selected.key : keys[0] || '';
  const activeValue = values[activeKey];

  const handleAdd = () => {
    let baseName = 'newValue';
    let count = 1;
    while (values[`${baseName}${count}`]) count++;
    const newKey = `${baseName}${count}`;

    setNamedValues({
      ...namedValues,
      values: { ...values, [newKey]: { val: 0.0 } },
    });
    setSelected({ store: 'values', key: newKey });
    setToast(`Added value "${newKey}"`);
  };

  const handleRename = (oldKey, newKey) => {
    if (!newKey || oldKey === newKey || values[newKey]) return;
    const newDict = { ...values };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, values: newDict });
    setSelected({ store: 'values', key: newKey });
  };

  const handleDelete = (keyToDelete) => {
    const newDict = { ...values };
    delete newDict[keyToDelete];
    setNamedValues({ ...namedValues, values: newDict });

    const remaining = Object.keys(newDict);
    setSelected({ store: 'values', key: remaining[0] || '' });
    setToast(`Deleted value "${keyToDelete}"`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
      {/* Left List */}
      <div className="md:col-span-1 border border-neutral-200 dark:border-neutral-800 rounded-xl p-4 bg-white dark:bg-neutral-900 flex flex-col space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-bold text-sm text-neutral-800 dark:text-neutral-200">
            Values Store ({Object.keys(values).length})
          </span>
          <button
            type="button"
            onClick={handleAdd}
            className="p-1.5 rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 text-xs font-semibold">
            <Plus className="w-3.5 h-3.5" /> Add
          </button>
        </div>

        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search values..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex-grow overflow-y-auto space-y-1 pr-1">
          {keys.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 py-6">
              No values found.
            </div>
          ) : (
            keys.map((k) => (
              <div
                key={k}
                onClick={() => setSelected({ store: 'values', key: k })}
                className={`p-2.5 rounded-lg text-xs cursor-pointer flex items-center justify-between transition-all ${
                  activeKey === k
                    ? 'bg-sky-100 dark:bg-sky-950/80 border border-sky-300 dark:border-sky-800 text-sky-900 dark:text-sky-200 font-semibold'
                    : 'hover:bg-neutral-100 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300'
                }`}>
                <span className="truncate">{k}</span>
                <span className="font-mono text-neutral-500 dark:text-neutral-400">
                  {values[k]?.val}
                </span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Detail Editor */}
      <div className="md:col-span-2 border border-neutral-200 dark:border-neutral-800 rounded-xl p-5 bg-white dark:bg-neutral-900 flex flex-col space-y-4">
        {activeKey && activeValue ? (
          <>
            <div className="flex items-center justify-between border-b border-neutral-200 dark:border-neutral-800 pb-3">
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  defaultValue={activeKey}
                  key={activeKey}
                  onBlur={(e) => handleRename(activeKey, e.target.value.trim())}
                  className="font-bold text-lg text-neutral-900 dark:text-neutral-100 bg-transparent border-b border-dashed border-neutral-400 focus:border-sky-500 outline-none px-1"
                />
                <span className="text-xs text-neutral-400 font-mono">
                  (Value Key)
                </span>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(activeKey)}
                className="p-1.5 rounded text-neutral-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                title="Delete Key">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-4 max-w-md">
              <div>
                <label className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider block mb-1">
                  Numerical Value
                </label>
                <input
                  type="number"
                  step="any"
                  value={activeValue.val}
                  onChange={(e) => {
                    const newVal = parseFloat(e.target.value) || 0;
                    setNamedValues({
                      ...namedValues,
                      values: { ...values, [activeKey]: { val: newVal } },
                    });
                  }}
                  className="w-full px-3 py-2 rounded-lg border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 focus:ring-2 focus:ring-sky-500 outline-none text-sm font-mono"
                />
              </div>
            </div>
          </>
        ) : (
          <div className="flex-grow flex items-center justify-center text-neutral-400 text-xs">
            Select or create a value to edit.
          </div>
        )}
      </div>
    </div>
  );
}

// Poses Store Editor
function PosesStoreEditor() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [selected, setSelected] = useAtom(selectedKeyAtom);
  const [search, setSearch] = useAtom(searchFilterAtom);
  const [, setToast] = useAtom(toastAtom);

  const poses = namedValues.poses || {};
  const keys = Object.keys(poses).filter((k) =>
    k.toLowerCase().includes(search.toLowerCase()),
  );

  const activeKey = selected.store === 'poses' ? selected.key : keys[0] || '';
  const activePose = poses[activeKey];

  const handleAdd = () => {
    let baseName = 'newPose';
    let count = 1;
    while (poses[`${baseName}${count}`]) count++;
    const newKey = `${baseName}${count}`;

    setNamedValues({
      ...namedValues,
      poses: {
        ...poses,
        [newKey]: {
          X: { val: 0 },
          Y: { val: 0 },
          Heading: { val: 0 },
          inRadians: false,
        },
      },
    });
    setSelected({ store: 'poses', key: newKey });
    setToast(`Added pose "${newKey}"`);
  };

  const handleRename = (oldKey, newKey) => {
    if (!newKey || oldKey === newKey || poses[newKey]) return;
    const newDict = { ...poses };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, poses: newDict });
    setSelected({ store: 'poses', key: newKey });
  };

  const handleDelete = (keyToDelete) => {
    const newDict = { ...poses };
    delete newDict[keyToDelete];
    setNamedValues({ ...namedValues, poses: newDict });

    const remaining = Object.keys(newDict);
    setSelected({ store: 'poses', key: remaining[0] || '' });
    setToast(`Deleted pose "${keyToDelete}"`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
      {/* Left List */}
      <div className="md:col-span-1 border border-neutral-200 dark:border-neutral-800 rounded-xl p-4 bg-white dark:bg-neutral-900 flex flex-col space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-bold text-sm text-neutral-800 dark:text-neutral-200">
            Poses Store ({Object.keys(poses).length})
          </span>
          <button
            type="button"
            onClick={handleAdd}
            className="p-1.5 rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 text-xs font-semibold">
            <Plus className="w-3.5 h-3.5" /> Add
          </button>
        </div>

        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search poses..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex-grow overflow-y-auto space-y-1 pr-1">
          {keys.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 py-6">
              No poses found.
            </div>
          ) : (
            keys.map((k) => {
              const res = resolvePoseRef({ ref: k }, namedValues);
              return (
                <div
                  key={k}
                  onClick={() => setSelected({ store: 'poses', key: k })}
                  className={`p-2.5 rounded-lg text-xs cursor-pointer flex items-center justify-between transition-all ${
                    activeKey === k
                      ? 'bg-sky-100 dark:bg-sky-950/80 border border-sky-300 dark:border-sky-800 text-sky-900 dark:text-sky-200 font-semibold'
                      : 'hover:bg-neutral-100 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300'
                  }`}>
                  <span className="truncate">{k}</span>
                  <span className="font-mono text-neutral-500 dark:text-neutral-400">
                    ({res.x.toFixed(1)}, {res.y.toFixed(1)})
                  </span>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* Right Detail Editor */}
      <div className="md:col-span-2 border border-neutral-200 dark:border-neutral-800 rounded-xl p-5 bg-white dark:bg-neutral-900 flex flex-col space-y-4 overflow-y-auto">
        {activeKey && activePose ? (
          <>
            <div className="flex items-center justify-between border-b border-neutral-200 dark:border-neutral-800 pb-3">
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  defaultValue={activeKey}
                  key={activeKey}
                  onBlur={(e) => handleRename(activeKey, e.target.value.trim())}
                  className="font-bold text-lg text-neutral-900 dark:text-neutral-100 bg-transparent border-b border-dashed border-neutral-400 focus:border-sky-500 outline-none px-1"
                />
                <span className="text-xs text-neutral-400 font-mono">
                  (Pose Key)
                </span>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(activeKey)}
                className="p-1.5 rounded text-neutral-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                title="Delete Pose">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <PoseRefControl
              label="Pose Coordinates & Heading"
              value={activePose}
              onChange={(updatedPose) => {
                setNamedValues({
                  ...namedValues,
                  poses: { ...poses, [activeKey]: updatedPose },
                });
              }}
            />
          </>
        ) : (
          <div className="flex-grow flex items-center justify-center text-neutral-400 text-xs">
            Select or create a pose to edit.
          </div>
        )}
      </div>
    </div>
  );
}

// Interpolators Store Editor
function InterpolatorsStoreEditor() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [selected, setSelected] = useAtom(selectedKeyAtom);
  const [search, setSearch] = useAtom(searchFilterAtom);
  const [, setToast] = useAtom(toastAtom);

  const interpolations = namedValues.interpolations || {};
  const keys = Object.keys(interpolations).filter((k) =>
    k.toLowerCase().includes(search.toLowerCase()),
  );

  const activeKey =
    selected.store === 'interpolations' ? selected.key : keys[0] || '';
  const activeInterp = interpolations[activeKey];

  const handleAdd = () => {
    let baseName = 'newInterpolator';
    let count = 1;
    while (interpolations[`${baseName}${count}`]) count++;
    const newKey = `${baseName}${count}`;

    setNamedValues({
      ...namedValues,
      interpolations: {
        ...interpolations,
        [newKey]: { reversed: false }, // TangentInterp default
      },
    });
    setSelected({ store: 'interpolations', key: newKey });
    setToast(`Added interpolator "${newKey}"`);
  };

  const handleRename = (oldKey, newKey) => {
    if (!newKey || oldKey === newKey || interpolations[newKey]) return;
    const newDict = { ...interpolations };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, interpolations: newDict });
    setSelected({ store: 'interpolations', key: newKey });
  };

  const handleDelete = (keyToDelete) => {
    const newDict = { ...interpolations };
    delete newDict[keyToDelete];
    setNamedValues({ ...namedValues, interpolations: newDict });

    const remaining = Object.keys(newDict);
    setSelected({ store: 'interpolations', key: remaining[0] || '' });
    setToast(`Deleted interpolator "${keyToDelete}"`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
      {/* Left List */}
      <div className="md:col-span-1 border border-neutral-200 dark:border-neutral-800 rounded-xl p-4 bg-white dark:bg-neutral-900 flex flex-col space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-bold text-sm text-neutral-800 dark:text-neutral-200">
            Interpolators ({Object.keys(interpolations).length})
          </span>
          <button
            type="button"
            onClick={handleAdd}
            className="p-1.5 rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 text-xs font-semibold">
            <Plus className="w-3.5 h-3.5" /> Add
          </button>
        </div>

        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search interpolators..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex-grow overflow-y-auto space-y-1 pr-1">
          {keys.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 py-6">
              No interpolators found.
            </div>
          ) : (
            keys.map((k) => (
              <div
                key={k}
                onClick={() => setSelected({ store: 'interpolations', key: k })}
                className={`p-2.5 rounded-lg text-xs cursor-pointer flex items-center justify-between transition-all ${
                  activeKey === k
                    ? 'bg-sky-100 dark:bg-sky-950/80 border border-sky-300 dark:border-sky-800 text-sky-900 dark:text-sky-200 font-semibold'
                    : 'hover:bg-neutral-100 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300'
                }`}>
                <span className="truncate">{k}</span>
                <span className="font-mono text-xs text-sky-600 dark:text-sky-400 font-normal">
                  {getInterpolatorType(interpolations[k])}
                </span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Detail Editor */}
      <div className="md:col-span-2 border border-neutral-200 dark:border-neutral-800 rounded-xl p-5 bg-white dark:bg-neutral-900 flex flex-col space-y-4 overflow-y-auto">
        {activeKey && activeInterp ? (
          <>
            <div className="flex items-center justify-between border-b border-neutral-200 dark:border-neutral-800 pb-3">
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  defaultValue={activeKey}
                  key={activeKey}
                  onBlur={(e) => handleRename(activeKey, e.target.value.trim())}
                  className="font-bold text-lg text-neutral-900 dark:text-neutral-100 bg-transparent border-b border-dashed border-neutral-400 focus:border-sky-500 outline-none px-1"
                />
                <span className="text-xs text-neutral-400 font-mono">
                  (Interpolator Key)
                </span>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(activeKey)}
                className="p-1.5 rounded text-neutral-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                title="Delete Interpolator">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <InterpRefControl
              label="Interpolator Definition"
              value={activeInterp}
              onChange={(updatedInterp) => {
                setNamedValues({
                  ...namedValues,
                  interpolations: {
                    ...interpolations,
                    [activeKey]: updatedInterp,
                  },
                });
              }}
            />
          </>
        ) : (
          <div className="flex-grow flex items-center justify-center text-neutral-400 text-xs">
            Select or create an interpolator to edit.
          </div>
        )}
      </div>
    </div>
  );
}

// Curves Store Editor
function CurvesStoreEditor() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [selected, setSelected] = useAtom(selectedKeyAtom);
  const [search, setSearch] = useAtom(searchFilterAtom);
  const [, setToast] = useAtom(toastAtom);

  const curves = namedValues.curves || {};
  const keys = Object.keys(curves).filter((k) =>
    k.toLowerCase().includes(search.toLowerCase()),
  );

  const activeKey = selected.store === 'curves' ? selected.key : keys[0] || '';
  const activeCurve = curves[activeKey];

  const handleAdd = () => {
    let baseName = 'newCurve';
    let count = 1;
    while (curves[`${baseName}${count}`]) count++;
    const newKey = `${baseName}${count}`;

    setNamedValues({
      ...namedValues,
      curves: {
        ...curves,
        [newKey]: {
          points: [
            {
              X: { val: 0 },
              Y: { val: 0 },
              Heading: { val: 0 },
              inRadians: false,
            },
          ],
          interpolation: { reversed: false },
        },
      },
    });
    setSelected({ store: 'curves', key: newKey });
    setToast(`Added curve "${newKey}"`);
  };

  const handleRename = (oldKey, newKey) => {
    if (!newKey || oldKey === newKey || curves[newKey]) return;
    const newDict = { ...curves };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, curves: newDict });
    setSelected({ store: 'curves', key: newKey });
  };

  const handleDelete = (keyToDelete) => {
    const newDict = { ...curves };
    delete newDict[keyToDelete];
    setNamedValues({ ...namedValues, curves: newDict });

    const remaining = Object.keys(newDict);
    setSelected({ store: 'curves', key: remaining[0] || '' });
    setToast(`Deleted curve "${keyToDelete}"`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
      {/* Left List */}
      <div className="md:col-span-1 border border-neutral-200 dark:border-neutral-800 rounded-xl p-4 bg-white dark:bg-neutral-900 flex flex-col space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-bold text-sm text-neutral-800 dark:text-neutral-200">
            Curves Store ({Object.keys(curves).length})
          </span>
          <button
            type="button"
            onClick={handleAdd}
            className="p-1.5 rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 text-xs font-semibold">
            <Plus className="w-3.5 h-3.5" /> Add
          </button>
        </div>

        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search curves..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex-grow overflow-y-auto space-y-1 pr-1">
          {keys.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 py-6">
              No curves found.
            </div>
          ) : (
            keys.map((k) => (
              <div
                key={k}
                onClick={() => setSelected({ store: 'curves', key: k })}
                className={`p-2.5 rounded-lg text-xs cursor-pointer flex items-center justify-between transition-all ${
                  activeKey === k
                    ? 'bg-sky-100 dark:bg-sky-950/80 border border-sky-300 dark:border-sky-800 text-sky-900 dark:text-sky-200 font-semibold'
                    : 'hover:bg-neutral-100 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300'
                }`}>
                <span className="truncate">{k}</span>
                <span className="font-mono text-neutral-500 dark:text-neutral-400">
                  {curves[k]?.points?.length || 0} pts
                </span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Detail Editor */}
      <div className="md:col-span-2 border border-neutral-200 dark:border-neutral-800 rounded-xl p-5 bg-white dark:bg-neutral-900 flex flex-col space-y-4 overflow-y-auto">
        {activeKey && activeCurve ? (
          <>
            <div className="flex items-center justify-between border-b border-neutral-200 dark:border-neutral-800 pb-3">
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  defaultValue={activeKey}
                  key={activeKey}
                  onBlur={(e) => handleRename(activeKey, e.target.value.trim())}
                  className="font-bold text-lg text-neutral-900 dark:text-neutral-100 bg-transparent border-b border-dashed border-neutral-400 focus:border-sky-500 outline-none px-1"
                />
                <span className="text-xs text-neutral-400 font-mono">
                  (Curve Key)
                </span>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(activeKey)}
                className="p-1.5 rounded text-neutral-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                title="Delete Curve">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <CurveRefControl
              label="Curve Definition"
              value={activeCurve}
              onChange={(updatedCurve) => {
                setNamedValues({
                  ...namedValues,
                  curves: { ...curves, [activeKey]: updatedCurve },
                });
              }}
            />
          </>
        ) : (
          <div className="flex-grow flex items-center justify-center text-neutral-400 text-xs">
            Select or create a curve to edit.
          </div>
        )}
      </div>
    </div>
  );
}

// Paths Store Editor
function PathsStoreEditor() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [selected, setSelected] = useAtom(selectedKeyAtom);
  const [search, setSearch] = useAtom(searchFilterAtom);
  const [, setToast] = useAtom(toastAtom);

  const paths = namedValues.paths || {};
  const keys = Object.keys(paths).filter((k) =>
    k.toLowerCase().includes(search.toLowerCase()),
  );

  const activeKey = selected.store === 'paths' ? selected.key : keys[0] || '';
  const activePath = paths[activeKey];

  const handleAdd = () => {
    let baseName = 'newPath';
    let count = 1;
    while (paths[`${baseName}${count}`]) count++;
    const newKey = `${baseName}${count}`;

    setNamedValues({
      ...namedValues,
      paths: {
        ...paths,
        [newKey]: {
          curves: [],
        },
      },
    });
    setSelected({ store: 'paths', key: newKey });
    setToast(`Added path "${newKey}"`);
  };

  const handleRename = (oldKey, newKey) => {
    if (!newKey || oldKey === newKey || paths[newKey]) return;
    const newDict = { ...paths };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, paths: newDict });
    setSelected({ store: 'paths', key: newKey });
  };

  const handleDelete = (keyToDelete) => {
    const newDict = { ...paths };
    delete newDict[keyToDelete];
    setNamedValues({ ...namedValues, paths: newDict });

    const remaining = Object.keys(newDict);
    setSelected({ store: 'paths', key: remaining[0] || '' });
    setToast(`Deleted path "${keyToDelete}"`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
      {/* Left List */}
      <div className="md:col-span-1 border border-neutral-200 dark:border-neutral-800 rounded-xl p-4 bg-white dark:bg-neutral-900 flex flex-col space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-bold text-sm text-neutral-800 dark:text-neutral-200">
            Paths Store ({Object.keys(paths).length})
          </span>
          <button
            type="button"
            onClick={handleAdd}
            className="p-1.5 rounded-md bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 text-xs font-semibold">
            <Plus className="w-3.5 h-3.5" /> Add
          </button>
        </div>

        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search paths..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-neutral-300 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800 text-neutral-900 dark:text-neutral-100 outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex-grow overflow-y-auto space-y-1 pr-1">
          {keys.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 py-6">
              No paths found.
            </div>
          ) : (
            keys.map((k) => (
              <div
                key={k}
                onClick={() => setSelected({ store: 'paths', key: k })}
                className={`p-2.5 rounded-lg text-xs cursor-pointer flex items-center justify-between transition-all ${
                  activeKey === k
                    ? 'bg-sky-100 dark:bg-sky-950/80 border border-sky-300 dark:border-sky-800 text-sky-900 dark:text-sky-200 font-semibold'
                    : 'hover:bg-neutral-100 dark:hover:bg-neutral-800 text-neutral-700 dark:text-neutral-300'
                }`}>
                <span className="truncate">{k}</span>
                <span className="font-mono text-neutral-500 dark:text-neutral-400">
                  {paths[k]?.curves?.length || 0} curves
                </span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Detail Editor */}
      <div className="md:col-span-2 border border-neutral-200 dark:border-neutral-800 rounded-xl p-5 bg-white dark:bg-neutral-900 flex flex-col space-y-4 overflow-y-auto">
        {activeKey && activePath ? (
          <>
            <div className="flex items-center justify-between border-b border-neutral-200 dark:border-neutral-800 pb-3">
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  defaultValue={activeKey}
                  key={activeKey}
                  onBlur={(e) => handleRename(activeKey, e.target.value.trim())}
                  className="font-bold text-lg text-neutral-900 dark:text-neutral-100 bg-transparent border-b border-dashed border-neutral-400 focus:border-sky-500 outline-none px-1"
                />
                <span className="text-xs text-neutral-400 font-mono">
                  (Path Sequence Key)
                </span>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(activeKey)}
                className="p-1.5 rounded text-neutral-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                title="Delete Path">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-neutral-700 dark:text-neutral-300 uppercase tracking-wider">
                  Path Sequence Curves
                </span>
                <button
                  type="button"
                  onClick={() => {
                    const curvesList = activePath.curves || [];
                    const updatedPath = {
                      ...activePath,
                      curves: [
                        ...curvesList,
                        {
                          points: [
                            {
                              X: { val: 0 },
                              Y: { val: 0 },
                              Heading: { val: 0 },
                              inRadians: false,
                            },
                          ],
                          interpolation: { reversed: false },
                        },
                      ],
                    };
                    setNamedValues({
                      ...namedValues,
                      paths: { ...paths, [activeKey]: updatedPath },
                    });
                  }}
                  className="px-2 py-1 text-xs rounded bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-1 font-semibold">
                  <Plus className="w-3.5 h-3.5" /> Add Curve
                </button>
              </div>

              {(activePath.curves || []).map((cRef, cIdx) => (
                <div key={cIdx} className="relative pt-1">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-bold text-sky-600 dark:text-sky-400">
                      Curve Segment #{cIdx + 1}
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        const newCurves = activePath.curves.filter(
                          (_, i) => i !== cIdx,
                        );
                        setNamedValues({
                          ...namedValues,
                          paths: {
                            ...paths,
                            [activeKey]: { ...activePath, curves: newCurves },
                          },
                        });
                      }}
                      className="text-neutral-400 hover:text-rose-500 transition-colors"
                      title="Remove Curve Segment">
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <CurveRefControl
                    label={`Segment ${cIdx + 1}`}
                    value={cRef}
                    onChange={(newCRef) => {
                      const newCurves = [...activePath.curves];
                      newCurves[cIdx] = newCRef;
                      setNamedValues({
                        ...namedValues,
                        paths: {
                          ...paths,
                          [activeKey]: { ...activePath, curves: newCurves },
                        },
                      });
                    }}
                  />
                </div>
              ))}

              <div className="pt-2 border-t border-neutral-200 dark:border-neutral-800">
                <InterpRefControl
                  label="Global Path Override Interpolator (Optional)"
                  value={activePath.globalInterpolator || { reversed: false }}
                  onChange={(globalInterpolator) => {
                    setNamedValues({
                      ...namedValues,
                      paths: {
                        ...paths,
                        [activeKey]: { ...activePath, globalInterpolator },
                      },
                    });
                  }}
                />
              </div>
            </div>
          </>
        ) : (
          <div className="flex-grow flex items-center justify-center text-neutral-400 text-xs">
            Select or create a path sequence to edit.
          </div>
        )}
      </div>
    </div>
  );
}

export default function App() {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [activeTab, setActiveTab] = useAtom(activeTabAtom);
  const [theme, setTheme] = useAtom(themeAtom);
  const [, setToast] = useAtom(toastAtom);

  // Statistics counters
  const counts = useMemo(
    () => ({
      values: Object.keys(namedValues.values || {}).length,
      poses: Object.keys(namedValues.poses || {}).length,
      interpolations: Object.keys(namedValues.interpolations || {}).length,
      curves: Object.keys(namedValues.curves || {}).length,
      paths: Object.keys(namedValues.paths || {}).length,
    }),
    [namedValues],
  );

  const loadPreset = (preset, name) => {
    setNamedValues(preset);
    setToast(`Loaded ${name} preset!`);
  };

  return (
    <div
      className={`${theme === 'dark' ? 'dark' : ''} min-h-screen font-sans antialiased select-none`}>
      <div className="min-h-screen bg-neutral-100 dark:bg-neutral-950 text-neutral-900 dark:text-neutral-100 flex flex-col">
        {/* Header Bar */}
        <header className="sticky top-0 z-30 bg-white/80 dark:bg-neutral-900/80 backdrop-blur-md border-b border-neutral-200 dark:border-neutral-800 px-6 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-sky-600 flex items-center justify-center text-white font-bold shadow-md shadow-sky-600/30">
              <Compass className="w-5 h-5" />
            </div>
            <div>
              <h1 className="font-bold text-base leading-tight tracking-tight">
                NamedValues Studio
              </h1>
              <p className="text-xs text-neutral-500 dark:text-neutral-400">
                Fluent Trajectory & Reference Graph Visualizer
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {/* Quick Stat Pills */}
            <div className="hidden lg:flex items-center gap-2 px-3 py-1 rounded-full bg-neutral-100 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700 text-xs font-mono">
              <span className="text-sky-600 dark:text-sky-400 font-bold">
                {counts.values}
              </span>{' '}
              Values
              <span className="text-neutral-400">|</span>
              <span className="text-sky-600 dark:text-sky-400 font-bold">
                {counts.poses}
              </span>{' '}
              Poses
              <span className="text-neutral-400">|</span>
              <span className="text-sky-600 dark:text-sky-400 font-bold">
                {counts.curves}
              </span>{' '}
              Curves
              <span className="text-neutral-400">|</span>
              <span className="text-sky-600 dark:text-sky-400 font-bold">
                {counts.paths}
              </span>{' '}
              Paths
            </div>

            {/* Presets Dropdown */}
            <select
              onChange={(e) => {
                if (e.target.value === 'autonomous')
                  loadPreset(SAMPLE_AUTONOMOUS_PRESET, 'Autonomous Trajectory');
                if (e.target.value === 'empty')
                  loadPreset(EMPTY_WORKSPACE_PRESET, 'Empty Workspace');
                e.target.value = '';
              }}
              className="px-3 py-1.5 text-xs rounded-md bg-white dark:bg-neutral-800 border border-neutral-300 dark:border-neutral-700 text-neutral-800 dark:text-neutral-200 focus:ring-2 focus:ring-sky-500 outline-none font-medium cursor-pointer">
              <option value="" disabled selected>
                Load Preset Template...
              </option>
              <option value="autonomous">Autonomous Trajectory Setup</option>
              <option value="empty">Clear / Empty Workspace</option>
            </select>

            {/* Theme Toggle Button */}
            <button
              type="button"
              onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
              className="p-2 rounded-md bg-neutral-100 dark:bg-neutral-800 border border-neutral-300 dark:border-neutral-700 text-neutral-600 dark:text-neutral-300 hover:text-sky-600 dark:hover:text-sky-400 transition-colors"
              title="Toggle Light/Dark Theme">
              {theme === 'dark' ? (
                <Sun className="w-4 h-4" />
              ) : (
                <Moon className="w-4 h-4" />
              )}
            </button>
          </div>
        </header>

        {/* Main Workspace Navigation Bar */}
        <div className="bg-white dark:bg-neutral-900 border-b border-neutral-200 dark:border-neutral-800 px-6 py-2 flex items-center justify-between overflow-x-auto">
          <nav className="flex items-center gap-1">
            {[
              {
                id: 'values',
                label: 'Values',
                count: counts.values,
                icon: Code,
              },
              {
                id: 'poses',
                label: 'Poses',
                count: counts.poses,
                icon: Compass,
              },
              {
                id: 'interpolations',
                label: 'Interpolators',
                count: counts.interpolations,
                icon: Move,
              },
              {
                id: 'curves',
                label: 'Curves',
                count: counts.curves,
                icon: Layers,
              },
              {
                id: 'paths',
                label: 'Paths',
                count: counts.paths,
                icon: ArrowRight,
              },
              { id: 'visualizer', label: '2D Field Canvas', icon: Eye },
              { id: 'json', label: 'Raw JSON Editor', icon: Code },
            ].map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-2 transition-all ${
                    isActive
                      ? 'bg-sky-600 text-white shadow-sm'
                      : 'text-neutral-600 dark:text-neutral-400 hover:bg-neutral-100 dark:hover:bg-neutral-800 hover:text-neutral-900 dark:hover:text-neutral-100'
                  }`}>
                  <Icon className="w-3.5 h-3.5" />
                  <span>{tab.label}</span>
                  {tab.count !== undefined && (
                    <span
                      className={`px-1.5 py-0.2 rounded-full font-mono text-[10px] ${
                        isActive
                          ? 'bg-white/20 text-white'
                          : 'bg-neutral-200 dark:bg-neutral-800 text-neutral-600 dark:text-neutral-400'
                      }`}>
                      {tab.count}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Workspace Body */}
        <main className="flex-grow p-6 overflow-hidden flex flex-col max-w-7xl w-full mx-auto">
          {activeTab === 'values' && <ValuesStoreEditor />}
          {activeTab === 'poses' && <PosesStoreEditor />}
          {activeTab === 'interpolations' && <InterpolatorsStoreEditor />}
          {activeTab === 'curves' && <CurvesStoreEditor />}
          {activeTab === 'paths' && <PathsStoreEditor />}
          {activeTab === 'visualizer' && <CanvasVisualizer />}
          {activeTab === 'json' && <JsonViewEditor />}
        </main>

        <NotificationToast />
      </div>
    </div>
  );
}
