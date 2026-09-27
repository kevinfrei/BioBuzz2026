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

// PoseRef Control: Switch between Inline ({ X, Y, Heading, inRadians }) and Ref ({ ref })
export function PoseRefControl({ label, value, onChange }): ReactElement {
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
