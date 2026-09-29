import { ReactElement, useMemo } from 'react';
import { Provider, useAtom, useAtomValue, useSetAtom } from 'jotai';

import {
  Button,
  FluentProvider,
  Select,
  SelectTabData,
  SelectTabEvent,
  Tab,
  TabList,
  Toolbar,
  webDarkTheme,
  webLightTheme,
} from '@fluentui/react-components';
import { WeatherMoonFilled, WeatherSunnyRegular } from '@fluentui/react-icons';

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
  themeAtom,
  toastAtom,
} from './state';
import { getStore } from './store';
import { ValuesEditor } from './ValuesEditor';

export function PedroPathsEditor(): ReactElement {
  const [namedValues, setNamedValues] = useAtom(namedValuesAtom);
  const [activeTab, setActiveTab] = useAtom(activeTabAtom);
  const [theme, setTheme] = useAtom(themeAtom);
  const setToast = useSetAtom(toastAtom);

  const onTabSelect = (event: SelectTabEvent, data: SelectTabData) => {
    setActiveTab(data.value as string);
  };

  // Statistics counters
  const counts = useMemo(
    () => ({
      values: Object.keys(namedValues.values).length,
      poses: Object.keys(namedValues.poses).length,
      interpolations: Object.keys(namedValues.interpolations).length,
      curves: Object.keys(namedValues.curves).length,
      paths: Object.keys(namedValues.paths).length,
    }),
    [namedValues],
  );

  const loadPreset = (preset: NamedValues, name: string) => {
    setNamedValues(preset);
    setToast(`Loaded ${name} preset!`);
  };

  return (
    <div>
      {/* Header Bar */}
      <Toolbar>
        <span style={{ marginRight: 10 }}>
          {counts.values} Values {counts.poses} Poses {counts.curves} Curves{' '}
          {counts.interpolations} Interpolations {counts.paths} Paths{' '}
        </span>

        {/* Presets Dropdown */}
        <Select
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
        </Select>

        {/* Theme Toggle Button */}
        <Button
          style={{ marginLeft: 10 }}
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
          title="Toggle Light/Dark Theme"
          icon={
            theme == 'dark' ? <WeatherSunnyRegular /> : <WeatherMoonFilled />
          }
        />
      </Toolbar>

      {/* Main Workspace Navigation Bar */}
      <div>
        <TabList selectedValue={activeTab} onTabSelect={onTabSelect}>
          <Tab value="values">Values</Tab>
          <Tab value="poses">Poses</Tab>
          <Tab value="interpolations">Headings</Tab>
          <Tab value="curves">Curves &amp; Lines</Tab>
          <Tab value="paths">Paths</Tab>
          <Tab value="visualizer">Field</Tab>
          <Tab value="json">JSON view</Tab>
        </TabList>
        {activeTab === 'values' && <ValuesEditor />}
        {activeTab === 'poses' && <PosesEditor />}
        {activeTab === 'interpolations' && <InterpolatorsEditor />}
        {activeTab === 'curves' && <CurvesEditor />}
        {activeTab === 'paths' && <PathsEditor />}
        {activeTab === 'visualizer' && <div>Put the old viz here</div>}
        {activeTab === 'json' && <JsonEditor />}
      </div>

      {/* Workspace Body */}
      <NotificationToast />
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
