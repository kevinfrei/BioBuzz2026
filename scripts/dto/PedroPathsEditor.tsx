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

export function PedroPathsEditor(): ReactElement {
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
