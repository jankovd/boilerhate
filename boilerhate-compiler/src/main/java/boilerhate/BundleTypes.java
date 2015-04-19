package boilerhate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.util.HashMap;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class BundleTypes {
  private static final ImmutableMap<String, String> SEALED_TYPES;
  private static final ImmutableList<Assignable> ASSIGNABLE_TYPES;

  private final Elements elements;
  private final Types types;

  public BundleTypes(Elements elements, Types types) {
    this.elements = elements;
    this.types = types;
  }

  /** @throws UnsupportedTypeException */
  public TypeMethods get(TypeMirror type) {
    final String name = type.toString();
    final String methodSuffix = SEALED_TYPES.get(name);
    if (methodSuffix != null) {
      return new TypeMethods(methodSuffix);
    }
    final List<Assignable> assignableTypes = ASSIGNABLE_TYPES;
    for (final Assignable assignableType : assignableTypes) {
      if (assignableType.isAssignableFrom(type, types, elements)) {
        final TypeMethods methods = new TypeMethods(assignableType.methodSuffix);
        if (assignableType.shouldAlwaysCastGetMethodQuery()) {
          methods.setAlwaysCastGetMethodQuery();
        }
        return methods;
      }
    }
    throw new UnsupportedTypeException(name);
  }

  public static class UnsupportedTypeException extends RuntimeException {
    public UnsupportedTypeException(String typename) {
      super(typename + " cannot be stored in a Bundle.");
    }
  }

  public static class TypeMethods {
    private static final String GET_METHOD = "get";
    private static final String GET_METHOD_PREFIX = "get";
    private static final String PUT_METHOD_PREFIX = "put";

    private final String getMethod;
    private String putMethod;
    private boolean alwaysCastGetMethodQuery;

    private TypeMethods(String methodSuffix) {
      this.putMethod = PUT_METHOD_PREFIX + methodSuffix;
      this.getMethod = GET_METHOD_PREFIX + methodSuffix;
    }

    private void setAlwaysCastGetMethodQuery() {
      alwaysCastGetMethodQuery = true;
    }

    public boolean shouldAlwaysCastGetMethodQuery() {
      return alwaysCastGetMethodQuery;
    }

    public String putMethod() {
      return putMethod;
    }

    public void addPutMethod(CodeBlock.Builder codeBlock, String bundle, String key,
        String targetName, String valueName) {
      codeBlock.add("$L.$L($S, $L.$L)", bundle, putMethod, key, targetName, valueName);
    }

    public String getMethod() {
      return getMethod;
    }

    public void addGetMethod(CodeBlock.Builder codeBlock, TypeName type, String bundle,
        String key) {
      if (alwaysCastGetMethodQuery) {
        codeBlock.add("($T) $L.$L($S)", type, bundle, getMethod, key);
      } else {
        codeBlock.add("$L.$L($S)", bundle, getMethod, key);
      }
    }
  }

  private static abstract class Assignable {
    public final String methodSuffix;
    private TypeMirror typeMirror;
    private boolean alwaysCastGetMethodQuery;

    private Assignable(String methodSuffix) {
      this.methodSuffix = methodSuffix;
    }

    public final boolean isAssignableFrom(TypeMirror type, Types typeUtils, Elements elementUtils) {
      if (typeMirror == null) {
        typeMirror = createTypeMirror(typeUtils, elementUtils);
      }
      return typeUtils.isAssignable(type, typeMirror);
    }

    public Assignable alwaysCastGetMethodQuery() {
      alwaysCastGetMethodQuery = true;
      return this;
    }

    public boolean shouldAlwaysCastGetMethodQuery() {
      return alwaysCastGetMethodQuery;
    }

    protected abstract TypeMirror createTypeMirror(Types typeUtils, Elements elementUtils);
  }

  private static class RawAssignable extends Assignable {
    private final String typeName;

    public RawAssignable(String typeName, String methodSuffix) {
      super(methodSuffix);
      this.typeName = typeName;
    }

    @Override protected TypeMirror createTypeMirror(Types typeUtils, Elements elementUtils) {
      return elementUtils.getTypeElement(typeName).asType();
    }
  }

  private static class ArrayAssignable extends Assignable {
    private final String typeName;

    private ArrayAssignable(String typeName, String methodSuffix) {
      super(methodSuffix);
      this.typeName = typeName;
    }

    @Override protected TypeMirror createTypeMirror(Types typeUtils, Elements elementUtils) {
      final TypeElement element = elementUtils.getTypeElement(typeName);
      return typeUtils.getArrayType(element.asType());
    }
  }

  private static class ParametrizedAssignable extends Assignable {
    private final String typeName;
    private final String extendsTypeName;

    private ParametrizedAssignable(String typeName, String extendsTypeName, String methodSuffix) {
      super(methodSuffix);
      this.typeName = typeName;
      this.extendsTypeName = extendsTypeName;
    }

    @Override protected TypeMirror createTypeMirror(Types typeUtils, Elements elementUtils) {
      final TypeElement container = elementUtils.getTypeElement(typeName);
      final TypeMirror extendsBounds = elementUtils.getTypeElement(extendsTypeName).asType();
      return typeUtils.getDeclaredType(container, typeUtils.getWildcardType(extendsBounds, null));
    }
  }

  static {
    HashMap<String, String> sealed = new HashMap<String, String>(40);
    sealed.put("boolean", "Boolean");
    sealed.put("boolean[]", "BooleanArray");
    sealed.put("java.lang.Boolean", "Boolean");
    sealed.put("byte", "Byte");
    sealed.put("byte[]", "ByteArray");
    sealed.put("java.lang.Byte", "Byte");
    sealed.put("short", "Short");
    sealed.put("short[]", "ShortArray");
    sealed.put("java.lang.Short", "Short");
    sealed.put("char", "Char");
    sealed.put("java.lang.Character", "Char");
    sealed.put("char[]", "CharArray");
    sealed.put("int", "Int");
    sealed.put("int[]", "IntArray");
    sealed.put("java.lang.Integer", "Int");
    sealed.put("long", "Long");
    sealed.put("long[]", "LongArray");
    sealed.put("java.lang.Long", "Long");
    sealed.put("float", "Float");
    sealed.put("float[]", "FloatArray");
    sealed.put("java.lang.Float", "Float");
    sealed.put("double", "Double");
    sealed.put("double[]", "DoubleArray");
    sealed.put("java.lang.Double", "Double");
    sealed.put("java.lang.String", "String");
    sealed.put("java.lang.String[]", "StringArray");
    sealed.put("android.os.Bundle", "Bundle");
    sealed.put("android.util.Size", "Size");
    sealed.put("android.util.SizeF", "SizeF");
    sealed.put("java.lang.CharSequence", "CharSequence");
    sealed.put("java.lang.CharSequence[]", "CharSequenceArray");
    sealed.put("android.os.Parcelable", "Parcelable");
    sealed.put("android.os.Parcelable[]", "ParcelableArray");
    SEALED_TYPES = ImmutableMap.copyOf(sealed);

    ASSIGNABLE_TYPES = ImmutableList.<Assignable>builder()
        .add(new RawAssignable("java.lang.CharSequence", "CharSequence"))
        .add(new ArrayAssignable("java.lang.CharSequence", "CharSequenceArray")
            .alwaysCastGetMethodQuery())
        .add(new RawAssignable("android.os.Parcelable", "Parcelable"))
        .add(new ArrayAssignable("android.os.Parcelable", "ParcelableArray")
            .alwaysCastGetMethodQuery())
        .add(new ParametrizedAssignable("java.util.ArrayList", "java.lang.String",
            "StringArrayList"))
        .add(new ParametrizedAssignable("java.util.ArrayList", "java.lang.Integer",
            "IntegerArrayList"))
        .add(new ParametrizedAssignable("java.util.ArrayList", "android.os.Parcelable",
            "ParcelableArrayList"))
        .add(new ParametrizedAssignable("android.util.SparseArray", "android.os.Parcelable",
            "SparseParcelableArray"))
        .add(new RawAssignable("java.io.Serializable", "Serializable")
            .alwaysCastGetMethodQuery())
        .build();
  }
}
