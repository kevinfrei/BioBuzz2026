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

// Interpolators Store Editor
export function InterpolatorsEditor(): ReactElement {
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
