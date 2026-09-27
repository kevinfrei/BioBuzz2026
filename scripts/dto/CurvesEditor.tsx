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

import { CurveRefControl } from './CurveRefControl';
import { Curve, CurveRef, resolvePoseRef } from './dto_schema';
import {
  EMPTY_WORKSPACE_PRESET,
  namedValuesAtom,
  SAMPLE_AUTONOMOUS_PRESET,
  searchFilterAtom,
  selectedKeyAtom,
  symbolTableAtom,
  toastAtom,
} from './state';

// Curves Store Editor
export function CurvesEditor(): ReactElement {
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

  const handleRename = (oldKey: string, newKey: string) => {
    if (!newKey || oldKey === newKey || curves[newKey]) return;
    const newDict = { ...curves };
    newDict[newKey] = newDict[oldKey];
    delete newDict[oldKey];

    setNamedValues({ ...namedValues, curves: newDict });
    setSelected({ store: 'curves', key: newKey });
  };

  const handleDelete = (keyToDelete: string) => {
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
              onChange={(updatedCurve: Curve) => {
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
