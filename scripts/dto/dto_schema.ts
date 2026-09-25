import {
  chkAnyOf,
  chkArrayOf,
  chkObjectOfExactType,
  isBoolean,
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
  logWay: boolean;
};
export type TangentInterp = { reversed: boolean };
export type InterpPiece = { until: ValRef; interpolator: InterpRef };
export type ResolvedInterpPiece = {
  until: ResolvedValue;
  interpolater: ResolvedInterpolator;
};
export type PieceWiseInterp = { pieces: InterpPiece[] };
export type ResolvedPieceWiseInterp = { pieces: ResolvedInterpolator[] };

export type Interpolator =
  ConstInterp | FacePtInterp | LinearInterp | PieceWiseInterp | TangentInterp;
export type InterpRef = Interpolator | Ref;
export type ResolvedInterpolator =
  | ResolvedConstInterp
  | ResolvedFacePtInterp
  | ResolvedLinearInterp
  | TangentInterp
  | ResolvedInterpPiece
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
