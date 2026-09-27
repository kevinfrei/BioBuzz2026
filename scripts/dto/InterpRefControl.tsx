import {
  ReactElement,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { useAtom, useAtomValue } from 'jotai';

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

import { resolvePoseRef } from './dto_schema';
import {
  EMPTY_WORKSPACE_PRESET,
  namedValuesAtom,
  SAMPLE_AUTONOMOUS_PRESET,
  symbolTableAtom,
  toastAtom,
} from './state';

export function InterpRefControl({ label, value, onChange }): ReactElement {
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
