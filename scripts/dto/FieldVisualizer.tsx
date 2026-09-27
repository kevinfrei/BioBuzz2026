import {
  ReactElement,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { useAtom } from 'jotai';

import { RefreshCw, ZoomIn, ZoomOut } from 'lucide-react';

import { resolveCurveRef, resolvePath, resolvePoseRef } from './Resolvers';
import {
  namedValuesAtom,
  selectedKeyAtom,
  visualizerSettingsAtom,
} from './state';

export function FieldVisualizer(): ReactElement {
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
