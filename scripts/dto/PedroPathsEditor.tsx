import { ReactElement, useMemo, useState } from 'react';
import { Provider, useAtom, useAtomValue, useSetAtom } from 'jotai';

import {
  FluentProvider,
  SelectTabData,
  SelectTabEvent,
  Tab,
  TabList,
  Toolbar,
  webDarkTheme,
  webLightTheme,
} from '@fluentui/react-components';
import {
  ArrowRight,
  Code,
  Compass,
  Eye,
  Layers,
  Moon,
  Move,
  Sun,
} from 'lucide-react';

import { CurvesEditor } from './CurvesEditor';
import { NamedValues } from './dto_schema';
import { InterpolatorsEditor } from './InterpolatorsEditor';
import { JsonEditor } from './JsonEditor';
import { NotificationToast } from './NotificationToast';
import { PathsEditor } from './PathsEditor';
import { PosesEditor } from './PosesEditor';
import {
  activeTabAtom,
  EMPTY_WORKSPACE_PRESET,
  namedValuesAtom,
  SAMPLE_AUTONOMOUS_PRESET,
  symbolTableAtom,
  themeAtom,
  toastAtom,
} from './state';
import { getStore } from './store';
import { ValuesEditor } from './ValuesEditor';

export function PedroPathsEditor(): ReactElement {
  const setNamedValues = useSetAtom(namedValuesAtom);
  const symbolTable = useAtomValue(symbolTableAtom);
  const [activeTab, setActiveTab] = useAtom(activeTabAtom);
  const [theme, setTheme] = useAtom(themeAtom);
  const setToast = useSetAtom(toastAtom);

  const [selectedTab, setSelectedTab] = useState<string>('values');
  const onTabSelect = (event: SelectTabEvent, data: SelectTabData) => {
    setSelectedTab(data.value as string);
  };

  // Statistics counters
  const counts = useMemo(
    () => ({
      values: symbolTable.values.size,
      poses: symbolTable.poses.size,
      interpolations: symbolTable.interpolations.size,
      curves: symbolTable.curves.size,
      paths: symbolTable.paths.size,
    }),
    [symbolTable],
  );

  const loadPreset = (preset: NamedValues, name: string) => {
    setNamedValues(preset);
    setToast(`Loaded ${name} preset!`);
  };

  return (
    <div
      className={`${theme === 'dark' ? 'dark' : ''} min-h-screen font-sans antialiased select-none`}>
      <div className="min-h-screen bg-neutral-100 dark:bg-neutral-950 text-neutral-900 dark:text-neutral-100 flex flex-col">
        {/* Header Bar */}
        <Toolbar>
          <span>
            <span>{counts.values} Values </span>
            <span>{counts.poses} Poses </span>
            <span>{counts.curves} Curves </span>
            <span>{counts.paths} Paths </span>
          </span>

          {/* Presets Dropdown */}
          <select
            onChange={(e) => {
              if (e.target.value === 'autonomous')
                loadPreset(SAMPLE_AUTONOMOUS_PRESET, 'Autonomous Trajectory');
              if (e.target.value === 'empty')
                loadPreset(EMPTY_WORKSPACE_PRESET, 'Empty Workspace');
              e.target.value = '';
            }}>
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
        </Toolbar>

        {/* Main Workspace Navigation Bar */}
        <main className="flex-grow p-6 overflow-hidden flex flex-col max-w-7xl w-full mx-auto">
          <TabList selectedValue={selectedTab} onTabSelect={onTabSelect}>
            <Tab value="values">Values</Tab>
            <Tab value="poses">Poses</Tab>
            <Tab value="interpolations">Headings</Tab>
            <Tab value="curves">Curves &amp; Lines</Tab>
            <Tab value="paths">Paths</Tab>
            <Tab value="visualizer">Field</Tab>
            <Tab value="json">JSON view</Tab>
          </TabList>
          {selectedTab === 'values' && <ValuesEditor />}
          {selectedTab === 'poses' && <PosesEditor />}
          {selectedTab === 'interpolations' && <InterpolatorsEditor />}
          {selectedTab === 'curves' && <CurvesEditor />}
          {selectedTab === 'paths' && <PathsEditor />}
          {selectedTab === 'visualizer' && <div>Put the old viz here</div>}
          {selectedTab === 'json' && <JsonEditor />}
        </main>

        {/* Workspace Body */}
        <NotificationToast />
      </div>
    </div>
  );
}

function ThemedApp(): ReactElement {
  const theme = useAtomValue(themeAtom);
  return (
    <FluentProvider theme={theme === 'dark' ? webDarkTheme : webLightTheme}>
      <PedroPathsEditor />
    </FluentProvider>
  );
}

export function App(): ReactElement {
  return (
    <Provider store={getStore()}>
      <ThemedApp />
    </Provider>
  );
}
