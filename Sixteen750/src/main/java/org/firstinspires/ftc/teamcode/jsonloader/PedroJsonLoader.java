package org.firstinspires.ftc.teamcode.jsonloader;

import android.content.Context;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pedropathing.api.Paths;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.curves.Curve;
import com.pedropathing.paths.curves.Line;
import com.pedropathing.paths.curves.bezier.BezierCurve;
import com.pedropathing.paths.interpolator.Interpolator;
import com.pedropathing.paths.interpolator.PiecewiseInterpolator;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class PedroJsonLoader {

    private static NamedItems dto;
    private static PedroItems items;

    // TODO: add loading from the 'upload only the paths' path, too...
    public static boolean loadPathFromJson(Context context) {
        try (
            Reader reader = new InputStreamReader(context.getAssets().open("paths/pedropaths.json"))
        ) {
            Gson gson = new Gson();
            dto = gson.fromJson(reader, NamedItems.class);
            items = new PedroItems(dto);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Path getPath(String name) {
        return items.paths.get(name);
    }

    public static Path getCurve(String name) {
        return items.curves.get(name);
    }

    public static Pose getPose(String name) {
        return items.poses.get(name);
    }

    public static Double getValue(String name) {
        return items.values.get(name);
    }

    public static void main(String[] args) {
        /*
        DTOValue dtoValue = new DTOValue(Math.PI);
        DTOValue dtoValue2 = new DTOValue(Math.E);
        DTOValue zero = new DTOValue(0.0);
        DTOPose dtoPose = new DTOPose(dtoValue, dtoValue2, zero, true);
        DTOPose dtoOther = new DTOPose("v1", "v2", "v3", true);
        List<RValue<DTOPose>> points = new ArrayList<>();
        points.add(new RValue<>(dtoPose));
        points.add(new RValue<DTOPose>("pose"));
        points.add(new RValue<>(dtoOther));
        FacingPoint facingPoint = new FacingPoint("facing");
        RValue<Interpolator> interpolation = new RValue<Interpolator>("pose");
        RValue<Interpolator> interpolation2 = new RValue<Interpolator>(facingPoint);
        DTOCurve dtoCurve = new DTOCurve(points, interpolation);
        dtoCurve.interpolation = interpolation;
        DTOPath dtoPath = new DTOPath();
        dtoPath.curves = new ArrayList<>();
        dtoPath.curves.add(new RValue<>(dtoCurve));
        dtoPath.globalInterpolator = interpolation;
        NamedItems items = new NamedItems();
        items.values = new HashMap<>();
        items.values.put("v1", dtoValue);
        items.values.put("v2", dtoValue2);
        items.values.put("v3", zero);
        items.poses = new HashMap<>();
        items.poses.put("pose", dtoPose);
        items.poses.put("otherPose", dtoOther);
        items.curves = new HashMap<>();
        items.curves.put("curve", dtoCurve);
        items.interpolations = new HashMap<>();
        items.interpolations.put("facing", facingPoint);
        items.paths = new HashMap<>();
        items.paths.put("aPath", dtoPath);
        */
        Gson gson = new Gson();
        String vals =
            "{\n" +
            "  \"values\": {\n" +
            "    \"v1\": { \"val\": 3.141592653589793 },\n" +
            "    \"v2\": { \"val\": 2.718281828459045 },\n" +
            "    \"v3\": { \"val\": 0.0 },\n" +
            "    \"v4\": { \"ref\": \"v2\" }\n" +
            "  },\n" +
            "  \"poses\": {\n" +
            "    \"p1\": {\n" +
            "      \"X\": { \"ref\": \"v1\" },\n" +
            "      \"Y\": { \"ref\": \"v2\" },\n" +
            "      \"Heading\": { \"ref\": \"p2\" }\n" +
            "    },\n" +
            "    \"p2\": {\n" +
            "      \"X\": { \"val\": 3.141592653589793 },\n" +
            "      \"Y\": { \"val\": 2.718281828459045 },\n" +
            "      \"Heading\": { \"angle\":{\"ref\": \"v3\" }, \"inRadians\": true }\n" +
            "    },\n" +
            "    \"p3\" : { \"ref\": \"p1\" }\n" +
            "  },\n" +
            "  \"curves\": {\n" +
            "    \"c1\": {\n" +
            "      \"points\": [\n" +
            "        {\n" +
            "          \"X\": { \"val\": 3.141592653589793 },\n" +
            "          \"Y\": { \"val\": 2.718281828459045 },\n" +
            "          \"Heading\": { \"angle\": {\"val\": 0.0 }, \"inRadians\": true }\n" +
            "        },\n" +
            "        { \"ref\": \"p1\" },\n" +
            "        {\n" +
            "          \"X\": { \"ref\": \"v1\" },\n" +
            "          \"Y\": { \"ref\": \"v2\" },\n" +
            "          \"Heading\": {\"angle\": { \"ref\": \"v1\" }, \"inRadians\": true }\n" +
            "        }\n" +
            "      ],\n" +
            "      \"interpolation\": { \"ref\": \"p1\" }\n" +
            "    },\n" +
            "    \"refCurve\": {\"ref\":\"c1\"}\n" +
            "  },\n" +
            "  \"interpolations\": {\n" +
            "    \"i1\": { \"point\": { \"ref\": \"p1\" }, \"type\": \"FacingPoint\" },\n" +
            "    \"i2\": {\"ref\": \"i1\"}\n" +
            "  },\n" +
            "  \"paths\": {\n" +
            "    \"path1\": {\n" +
            "      \"curves\": [\n" +
            "        {\n" +
            "          \"points\": [\n" +
            "            {\n" +
            "              \"X\": { \"val\": 3.141592653589793 },\n" +
            "              \"Y\": { \"val\": 2.718281828459045 },\n" +
            "              \"heading\": {\"angle\":{ \"val\": 0.0 }, \"inRadians\": false}\n" +
            "            },\n" +
            "            { \"ref\": \"p1\" },\n" +
            "            {\n" +
            "              \"X\": { \"ref\": \"v1\" },\n" +
            "              \"Y\": { \"ref\": \"v2\" },\n" +
            "              \"heading\": {\"angle\":{ \"ref\": \"v3\" }, \"inRadians\": true }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"interpolation\": { \"ref\": \"i1\" }\n" +
            "        },\n" +
            "        {\"ref\":\"c2\"}\n" +
            "      ],\n" +
            "      \"globalInterpolator\": { \"ref\": \"i2\" }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        // gson.toJson(items);
        NamedItems roundTrip = gson.fromJson(vals, NamedItems.class);
        System.out.printf("%d%n", roundTrip.values.size());
        System.out.printf("%d%n", roundTrip.poses.size());
        System.out.printf("%d%n", roundTrip.curves.size());
        System.out.printf("%d%n", roundTrip.interpolations.size());
        System.out.printf("%d%n", roundTrip.paths.size());
    }

    static class NamedItems {

        public Map<String, RValue<DTOValue>> values;
        public Map<String, RValue<DTOPose>> poses;
        public Map<String, RValue<DTOInterpolator>> interpolations;
        public Map<String, RValue<DTOCurve>> curves;
        public Map<String, DTOPath> paths;
    }

    static class Piece {

        public double until;
        public InterpDescr interp;

        public Piece(double until, InterpDescr id) {
            this.until = until;
            interp = id;
        }
    }

    static class InterpDescr {

        public String type;
        public boolean revOrLongWay;
        public double heading1X, heading2Y;
        public Piece[] pieces;

        /// Tangent
        public InterpDescr(boolean rev) {
            type = "tangent";
            revOrLongWay = rev;
        }

        /// Constant
        public InterpDescr(double heading) {
            type = "constant";
            heading1X = heading;
        }

        /// Linear
        public InterpDescr(double start, double end, boolean longWay) {
            type = "linear";
            revOrLongWay = longWay;
            heading1X = start;
            heading2Y = end;
        }

        /// Facing
        public InterpDescr(double x, double y) {
            type = "facing";
            heading1X = x;
            heading2Y = y;
        }

        /// Piece-wise
        public InterpDescr(Piece[] pieceList) {
            type = "pieces";
            pieces = pieceList;
        }
    }

    static class PedroItems {

        Map<String, Double> values;
        Map<String, Pose> poses;
        Map<String, InterpDescr> interpolations;
        Map<String, Path> curves;
        Map<String, Path> paths;
        List<String> errors;

        public PedroItems(NamedItems dto) {
            errors = new ArrayList<>();
            EvalValues(dto);
            EvalPoses(dto);
            EvalInterpolations(dto);
            EvalCurves(dto);
            EvalPaths(dto);
        }

        private static <T> void TransitiveAssignment(
            Map<String, T> res,
            Map<String, List<String>> pending,
            String name,
            T value
        ) {
            // Transitively set any pending value refs
            Stack<String> resolve = new Stack<>();
            resolve.push(name);
            do {
                name = resolve.pop();
                res.put(name, value);
                List<String> toAssign = pending.get(name);
                if (toAssign != null) {
                    for (String n : toAssign) {
                        resolve.push(n);
                    }
                    pending.remove(name);
                }
            } while (!resolve.empty());
        }

        Double getValue(RValue<DTOValue> valRef) {
            if (valRef.isRef()) {
                return values.get(valRef.getRef());
            } else {
                return valRef.getValue().val;
            }
        }

        void EvalValues(NamedItems dto) {
            values = new HashMap<>(dto.values.size());
            Map<String, List<String>> pending = new HashMap<>(dto.values.size());
            for (Map.Entry<String, RValue<DTOValue>> rval : dto.values.entrySet()) {
                String name = rval.getKey();
                RValue<DTOValue> valRef = rval.getValue();
                Double evaluated = getValue(valRef);
                if (evaluated != null) {
                    // We've got the full value.
                    // Take care of it, and any transitive definitions.
                    TransitiveAssignment(values, pending, name, evaluated);
                } else {
                    // We haven't seen this value yet:
                    // Add the name to the pending list for the ref, and keep going
                    pending.computeIfAbsent(valRef.getRef(), k -> new ArrayList<>()).add(name);
                }
            }
            if (!pending.isEmpty()) {
                // TODO: Fill this in with more/better details
                errors.add("Errors evaluating values");
            }
        }

        Double getHeading(RValue<DTOHeading> heading) {
            if (heading.isRef()) {
                // It's a poseref;
                RValue<DTOPose> poseRef = new RValue<>();
                poseRef.ref = heading.getRef();
                Pose p = getPose(poseRef);
                if (p == null) {
                    return null;
                }
                return p.heading();
            }
            DTOHeading h = heading.getValue();
            Double val = getValue(h.angle);
            if (val != null) {
                return h.inRadians ? val : (val * Math.PI) / 180.0;
            }
            return null;
        }

        Pose getPose(RValue<DTOPose> poseRef) {
            if (poseRef.isRef()) {
                return poses.get(poseRef.getRef());
            } else {
                DTOPose dtoPose = poseRef.getValue();
                Double X = getValue(dtoPose.X);
                Double Y = getValue(dtoPose.Y);
                Double Heading = getHeading(dtoPose.heading);
                return X != null && Y != null && Heading != null ? new Pose(X, Y, Heading) : null;
            }
        }

        void EvalPoses(NamedItems dto) {
            poses = new HashMap<>(dto.poses.size());
            Map<String, List<String>> pending = new HashMap<>(dto.poses.size());
            for (Map.Entry<String, RValue<DTOPose>> rval : dto.poses.entrySet()) {
                String name = rval.getKey();
                RValue<DTOPose> poseRef = rval.getValue();
                Pose evaluated = getPose(poseRef);
                if (evaluated != null) {
                    // We've got the full value.
                    // Take care of it, and any transitive definitions.
                    TransitiveAssignment(poses, pending, name, evaluated);
                } else {
                    // We haven't seen this value yet:
                    // Add the name to the pending list for the ref, and keep going
                    pending.computeIfAbsent(poseRef.getRef(), k -> new ArrayList<>()).add(name);
                }
            }
            if (!pending.isEmpty()) {
                // TODO: Fill this in with more/better details
                errors.add("Errors evaluating poses");
            }
        }

        InterpDescr getInterpolator(RValue<DTOInterpolator> interpRef) {
            if (interpRef.isRef()) {
                return interpolations.get(interpRef.getRef());
            } else {
                DTOInterpolator value = interpRef.getValue();
                if (value instanceof DTOTangent) {
                    return new InterpDescr(((DTOTangent) value).reversed);
                } else if (value instanceof DTOLinear) {
                    DTOLinear dto = (DTOLinear) value;
                    Double startHeading = getHeading(dto.startHeading);
                    Double endHeading = getHeading(dto.endHeading);
                    if (startHeading != null && endHeading != null) {
                        return new InterpDescr(startHeading, endHeading, dto.longWay);
                    } else {
                        return null;
                    }
                } else if (value instanceof DTOConstant) {
                    DTOConstant dto = (DTOConstant) value;
                    Double heading = getHeading(dto.heading);
                    return heading == null ? null : new InterpDescr(heading);
                } else if (value instanceof DTOFacingPoint) {
                    DTOFacingPoint dto = (DTOFacingPoint) value;
                    Pose pose = getPose(dto.point);
                    return pose == null ? null : new InterpDescr(pose.x(), pose.y());
                } else if (value instanceof DTOPieceWise) {
                    DTOPieceWise dto = (DTOPieceWise) value;
                    Piece[] lst = new Piece[dto.pieces.size()];
                    int i = 0;
                    for (DTOPiecePortion p : dto.pieces) {
                        Double until = getValue(p.until);
                        InterpDescr id = getInterpolator(p.interpolator);
                        if (until == null || id == null) {
                            return null;
                        }
                        lst[i++] = new Piece(until, id);
                    }
                    return new InterpDescr(lst);
                }
            }
            return null;
        }

        void EvalInterpolations(NamedItems dto) {
            interpolations = new HashMap<>(dto.interpolations.size());
            Map<String, List<String>> pending = new HashMap<>(dto.interpolations.size());
            for (Map.Entry<String, RValue<DTOInterpolator>> rval : dto.interpolations.entrySet()) {
                String name = rval.getKey();
                RValue<DTOInterpolator> interpRef = rval.getValue();
                InterpDescr evaluated = getInterpolator(interpRef);
                if (evaluated != null) {
                    // We've got the full value.
                    // Take care of it, and any transitive definitions.
                    TransitiveAssignment(interpolations, pending, name, evaluated);
                } else {
                    // We haven't seen this value yet:
                    // Add the name to the pending list for the ref, and keep going
                    pending.computeIfAbsent(interpRef.ref, k -> new ArrayList<>()).add(name);
                }
            }
            if (!pending.isEmpty()) {
                // TODO: Fill this in with more/better details
                errors.add("Errors evaluating interpolators");
            }
        }

        Path getCurve(RValue<DTOCurve> curveRef) {
            if (curveRef.isRef()) {
                return curves.get(curveRef.getRef());
            } else {
                DTOCurve dtoCurve = curveRef.getValue();
                InterpDescr id = getInterpolator(dtoCurve.interpolation);
                Pose[] poses = new Pose[dtoCurve.points.size()];
                int i = 0;
                for (RValue<DTOPose> pose : dtoCurve.points) {
                    Pose p = getPose(pose);
                    if (p == null) {
                        return null;
                    }
                    poses[i++] = p;
                }
                if (id == null) {
                    return null;
                }
                Path p = null;
                if (poses.length == 2) {
                    p = Paths.line(poses[0], poses[1]);
                } else {
                    p = Paths.curve(poses);
                }
                switch (id.type) {
                    case "tangent":
                        return id.revOrLongWay ? p.reverseTangent() : p.tangent();
                    case "constant":
                        return p.constant(id.heading1X);
                    case "linear":
                        return p.linear(id.heading1X, id.heading2Y);
                    case "facing":
                        return p.facingPoint(new Pose(id.heading1X, id.heading2Y));
                    case "piece": {
                        /* TODO: Make this work
            PiecewiseInterpolator pi = Interpolator.piecewise();
            for (Piece piece: id.pieces) {
              pi = pi.until(piece.until, piece.interp);
            }
            */
                        return null;
                    }
                }
            }
            return null;
        }

        void EvalCurves(NamedItems dto) {
            curves = new HashMap<>(dto.curves.size());
            Map<String, List<String>> pending = new HashMap<>(dto.curves.size());
            for (Map.Entry<String, RValue<DTOCurve>> rval : dto.curves.entrySet()) {
                String name = rval.getKey();
                RValue<DTOCurve> curveRef = rval.getValue();
                Path evaluated = getCurve(curveRef);
                if (evaluated != null) {
                    // We've got the full value.
                    // Take care of it, and any transitive definitions.
                    TransitiveAssignment(curves, pending, name, evaluated);
                } else {
                    // We haven't seen this value yet:
                    // Add the name to the pending list for the ref, and keep going
                    pending.computeIfAbsent(curveRef.ref, k -> new ArrayList<>()).add(name);
                }
            }
            if (!pending.isEmpty()) {
                // TODO: Fill this in with more/better details
                errors.add("Errors evaluating curves");
            }
        }

        Map<String, Path> EvalPaths(NamedItems dto) {
            Map<String, Path> res = new HashMap<>(dto.paths.size());
            return res;
        }
    }

    @JsonAdapter(RValue.AdapterFactory.class)
    public static class RValue<T> {

        String ref;
        T value;

        public boolean isRef() {
            return ref != null && !ref.isEmpty();
        }

        public String getRef() {
            return ref;
        }

        public T getValue() {
            return value;
        }

        // 1. The Factory that catches generic RValue<T> instances
        public static class AdapterFactory implements TypeAdapterFactory {

            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                // Check if the type being parsed/written is assignable from RValue
                if (!RValue.class.isAssignableFrom(type.getRawType())) {
                    return null;
                }

                // Extract the inner type argument T (e.g., PoseDTO from RValue<PoseDTO>)
                Type societalType = type.getType();
                Type innerType = Object.class;
                if (societalType instanceof ParameterizedType) {
                    innerType = ((ParameterizedType) societalType).getActualTypeArguments()[0];
                }

                // Get Gson's built-in adapter for the inner type
                TypeAdapter<?> innerAdapter = gson.getAdapter(TypeToken.get(innerType));

                // Return our custom RValue adapter bound to this specific inner type
                return (TypeAdapter<T>) new RValueAdapter<>(innerAdapter);
            }
        }

        // 2. The actual Adapter that handles serialization and deserialization
        private static class RValueAdapter<T> extends TypeAdapter<RValue<T>> {

            private final TypeAdapter<T> innerAdapter;

            public RValueAdapter(TypeAdapter<T> innerAdapter) {
                this.innerAdapter = innerAdapter;
            }

            @Override
            public void write(JsonWriter out, RValue<T> value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                if (value.isRef()) {
                    out.beginObject();
                    out.name("ref").value(value.getRef());
                    out.endObject();
                } else {
                    // Delegate writing the inline value to its native adapter
                    innerAdapter.write(out, value.getValue());
                }
            }

            @Override
            public RValue<T> read(JsonReader in) throws IOException {
                // Parse incoming JSON into a tree structure for easy inspection
                JsonParser jp = new JsonParser();
                JsonElement element = jp.parse(in);
                RValue<T> rVal = new RValue<>();

                if (element.isJsonObject() && element.getAsJsonObject().has("ref")) {
                    rVal.ref = element.getAsJsonObject().get("ref").getAsString();
                } else {
                    // Delegate reading the inline value back to the inner adapter
                    rVal.value = innerAdapter.fromJsonTree(element);
                }

                return rVal;
            }
        }
    }

    public static class DTOValue {

        public double val;
    }

    // A Line is 2 points
    public static class DTOCurve {

        public List<RValue<DTOPose>> points;
        public RValue<DTOInterpolator> interpolation; // nullable
    }

    public static class DTOPath {

        public List<RValue<DTOCurve>> curves;
        public RValue<DTOInterpolator> globalInterpolator; // nullable
    }

    public static class DTOHeading {

        public RValue<DTOValue> angle;
        public boolean inRadians;
    }

    public static class DTOPose {

        public RValue<DTOValue> X;
        public RValue<DTOValue> Y;
        public RValue<DTOHeading> heading; // Nullable
    }

    // Heading Interpolators:

    public static class DTOConstant extends DTOInterpolator {

        @Override
        public String getType() {
            return "Constant";
        }

        public RValue<DTOHeading> heading;
    }

    public static class DTOFacingPoint extends DTOInterpolator {

        @Override
        public String getType() {
            return "FacingPoint";
        }

        public RValue<DTOPose> point;
    }

    public static class DTOLinear extends DTOInterpolator {

        @Override
        public String getType() {
            return "Linear";
        }

        public RValue<DTOHeading> startHeading;
        public RValue<DTOHeading> endHeading;
        public boolean longWay = false;
    }

    public static class DTOPiecePortion {

        public RValue<DTOValue> until;
        public RValue<DTOInterpolator> interpolator;
    }

    public static class DTOPieceWise extends DTOInterpolator {

        @Override
        public String getType() {
            return "PieceWise";
        }

        public List<DTOPiecePortion> pieces;
    }

    public static class DTOTangent extends DTOInterpolator {

        public String getType() {
            return "Tangent";
        }

        public boolean reversed = false;
    }

    @JsonAdapter(DTOInterpolator.AdapterFactory.class)
    public abstract static class DTOInterpolator {

        public abstract String getType();

        static class AdapterFactory implements TypeAdapterFactory {

            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (!DTOInterpolator.class.isAssignableFrom(type.getRawType())) {
                    return null;
                }

                // Delegate to runtime mapping or handle sub-typing logic here
                // This keeps your GsonBuilder completely clean of navigation rules.
                return (TypeAdapter<T>) new Adapter(gson);
            }
        }

        private static class Adapter extends TypeAdapter<DTOInterpolator> {

            private final Gson gson;

            // Gson passes itself into the adapter factory, which we forward here
            public Adapter(Gson gson) {
                this.gson = gson;
            }

            @Override
            public void write(JsonWriter out, DTOInterpolator value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }

                // 1. Let Gson convert the concrete class into a JsonElement tree
                JsonElement tree = gson.toJsonTree(value, value.getClass());
                JsonObject jsonObject = tree.getAsJsonObject();

                // 2. Inject the correct "type" discriminator based on the subclass
                if (value instanceof DTOTangent) {
                    jsonObject.addProperty("type", "Tangent");
                } else if (value instanceof DTOLinear) {
                    jsonObject.addProperty("type", "Linear");
                } else if (value instanceof DTOConstant) {
                    jsonObject.addProperty("type", "Constant");
                } else if (value instanceof DTOFacingPoint) {
                    jsonObject.addProperty("type", "FacingPoint");
                } else if (value instanceof DTOPieceWise) {
                    jsonObject.addProperty("type", "PieceWise");
                }

                // 3. Write the modified tree out to the stream
                gson.toJson(jsonObject, out);
            }

            @Override
            public DTOInterpolator read(JsonReader in) throws IOException {
                // 1. Parse the incoming stream into a JsonObject tree
                JsonParser jp = new JsonParser();
                JsonObject jsonObject = jp.parse(in).getAsJsonObject();

                if (!jsonObject.has("type")) {
                    throw new JsonParseException(
                        "Missing 'type' discriminator field for DTOInterpolator"
                    );
                }

                String type = jsonObject.get("type").getAsString();
                Class<? extends DTOInterpolator> targetClass;

                // 2. Map the string type to the correct concrete Java class
                switch (type) {
                    case "Tangent":
                        targetClass = DTOTangent.class;
                        break;
                    case "Linear":
                        targetClass = DTOLinear.class;
                        break;
                    case "Constant":
                        targetClass = DTOConstant.class;
                        break;
                    case "FacingPoint":
                        targetClass = DTOFacingPoint.class;
                        break;
                    case "PieceWise":
                        targetClass = DTOPieceWise.class;
                        break;
                    default:
                        throw new JsonParseException("Unknown heading interpolation type: " + type);
                }

                // 3. Delegate the object mapping back to Gson for the specific subclass
                return gson.fromJson(jsonObject, targetClass);
            }
        }
    }
}
