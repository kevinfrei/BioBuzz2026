import {
  chkAnyOf,
  chkArrayOf,
  chkObjectOfExactType,
  hasField,
  isBoolean,
  isDefined,
  isNumber,
  isString,
  typecheck,
} from '@freik/typechk';

export type Ref = { ref: string };
export type Err = { err: string };
export type Value = { val: number };
export type ValRef = Value | Ref;
export type ResolvedValue = number | Err;

export type Pose = {
  X: ValRef;
  Y: ValRef;
  Heading?: ValRef;
  inRadians?: boolean;
};
export type PoseRef = Pose | Ref;
export type CorrectPose = {
  X: ResolvedValue;
  Y: ResolvedValue;
  Heading?: ResolvedValue;
};
export type ResolvedPose = CorrectPose | Err;

export type ConstInterp = { heading: ValRef };
export type ResolvedConstInterp = { heading: ResolvedValue };
export type FacePtInterp = { point: PoseRef };
export type ResolvedFacePtInterp = { point: ResolvedPose };
export type LinearInterp = {
  startHeading: ValRef;
  endHeading: ValRef;
  longWay: boolean;
};
export type ResolvedLinearInterp = {
  startHeading: ResolvedValue;
  endHeading: ResolvedValue;
  longWay: boolean;
};
export type TangentInterp = { reversed: boolean };
export type InterpPiece = { until: ValRef; interpolator: InterpRef };
export type ResolvedInterpPiece = {
  until: ResolvedValue;
  interpolater: ResolvedInterpolator;
};
export type PieceWiseInterp = { pieces: InterpPiece[] };
export type ResolvedPieceWiseInterp = { pieces: ResolvedInterpPiece[] };

export type Interpolator =
  ConstInterp | FacePtInterp | LinearInterp | PieceWiseInterp | TangentInterp;
export type InterpRef = Interpolator | Ref;
export type ResolvedInterpolator =
  | ResolvedConstInterp
  | ResolvedFacePtInterp
  | ResolvedLinearInterp
  | TangentInterp
  | ResolvedPieceWiseInterp
  | Err;

export type Curve = { points: PoseRef[]; interpolation: InterpRef };
export type CurveRef = Curve | Ref;
export type CorrectCurve = {
  points: ResolvedPose[];
  interpolation: ResolvedInterpolator;
};
export type ResolvedCurve = CorrectCurve | Err;

export type Path = { curves: CurveRef[]; globalInterpolator?: InterpRef };
export type ResolvedPath = {
  curves: ResolvedCurve[];
  globalInterpolator?: ResolvedInterpolator;
};

export type NamedValues = {
  values?: Record<string, Value>;
  poses?: Record<string, Pose>;
  interpolations?: Record<string, Interpolator>;
  curves?: Record<string, Curve>;
  paths?: Record<string, Path>;
};
export type SymbolTable = {
  values: Map<string, Value>;
  poses: Map<string, Pose>;
  interpolations: Map<string, Interpolator>;
  curves: Map<string, Curve>;
  paths: Map<string, Path>;
};

// Type checkers:
export const chkRef = chkObjectOfExactType<Ref>({ ref: isString });
export const chkValue = chkObjectOfExactType<Value>({ val: isNumber });
export const chkValRef = chkAnyOf(chkRef, chkValue);
export const chkPose = chkObjectOfExactType<Pose>(
  {
    X: chkValRef,
    Y: chkValRef,
  },
  {
    Heading: chkValRef,
    inRadians: isBoolean,
  },
);
export const chkPoseRef = chkAnyOf(chkRef, chkPose);
export const chkConstInterp = chkObjectOfExactType<ConstInterp>({
  heading: chkValRef,
});
export const chkFacePtInterp = chkObjectOfExactType<FacePtInterp>({
  point: chkPoseRef,
});
export const chkLinearInterp = chkObjectOfExactType<LinearInterp>({
  startHeading: chkValRef,
  endHeading: chkValRef,
  longWay: isBoolean,
});
export const chkTangentInterp = chkObjectOfExactType<TangentInterp>({
  reversed: isBoolean,
});
// have to declare a function, because recursion and delayed binding and stuff
export const chkInterpPiece: typecheck<InterpPiece> = (obj: unknown) =>
  chkObjectOfExactType<InterpPiece>({
    until: chkValRef,
    interpolator: chkInterpRef,
  })(obj);
export const chkPieceWiseInterp = chkObjectOfExactType<PieceWiseInterp>({
  pieces: chkArrayOf(chkInterpPiece),
});
export const chkInterpolator: typecheck<Interpolator> = chkAnyOf(
  chkConstInterp,
  chkFacePtInterp,
  chkLinearInterp,
  chkPieceWiseInterp,
  chkTangentInterp,
);
export const chkInterpRef: typecheck<InterpRef> = (obj: unknown) =>
  chkAnyOf(chkRef, chkInterpolator)(obj);

export const chkCurve = chkObjectOfExactType<Curve>({
  points: chkArrayOf(chkPoseRef),
  interpolation: chkInterpRef,
});
export const chkCurveRef = chkAnyOf(chkRef, chkCurve);

export const chkPath = chkObjectOfExactType<Path>(
  { curves: chkArrayOf(chkCurveRef) },
  { globalInterpolator: chkInterpRef },
);

export const chkNamedValues = chkObjectOfExactType<NamedValues>(
  {},
  {
    values: chkArrayOf(chkValue),
    poses: chkArrayOf(chkPose),
    curves: chkArrayOf(chkCurve),
    interpolations: chkArrayOf(chkInterpolator),
    paths: chkArrayOf(chkPath),
  },
);

// Resolvers:

export function resolveValRef(
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

export function resolvePoseRef(
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
  if (hasField(poseRef, 'Heading')) {
    let Heading = resolveValRef(poseRef.Heading!, dict);
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

export function resolveInterpRef(
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

export function resolveCurveRef(
  curveRef: CurveRef,
  dict: SymbolTable,
  seen = new Set<string>(),
): ResolvedCurve {
  if (chkRef(curveRef)) {
    if (seen.has(curveRef.ref)) {
      return {
        err: `Circular curve reference detected :${curveRef.ref}`,
      };
    }
    seen.add(curveRef.ref);
    const lkup = dict.curves.get(curveRef.ref);
    return isDefined(lkup)
      ? resolveCurveRef(lkup, dict, seen)
      : { err: `Missing interpolator reference: ${curveRef.ref}` };
  }
  return {
    points: curveRef.points.map((poseRef) => resolvePoseRef(poseRef, dict)),
    interpolation: resolveInterpRef(curveRef.interpolation, dict),
  };
}

export function resolvePath(path: Path, dict: SymbolTable) {
  const curves = path.curves.map((curveRef) => resolveCurveRef(curveRef, dict));
  if (hasField(path, 'globalInterpolator')) {
    return {
      curves,
      globalInterpolator: resolveInterpRef(path.globalInterpolator!, dict),
    };
  }
  return { curves };
}
