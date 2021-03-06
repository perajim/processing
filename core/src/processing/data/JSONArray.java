package processing.data;

// This code has been modified heavily to more closely match the rest of the
// Processing API. In the spirit of the rest of the project, where we try to
// keep the API as simple as possible, we have erred on the side of being
// conservative in choosing which functions to include, since we haven't yet
// decided what's truly necessary. Power users looking for a full-featured
// version can use the original version from json.org, or one of the many
// other APIs that are available. As with all Processing API, if there's a
// function that should be added, please let use know, and have others vote:
// http://code.google.com/p/processing/issues/list

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;

import processing.core.PApplet;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2012-11-13
 * @webref data:composite
 * @see JSONObject
 * @see PApplet#loadJSONObject(String)
 * @see PApplet#loadJSONArray(String)
 * @see PApplet#saveJSONObject(JSONObject, String)
 * @see PApplet#saveJSONArray(JSONArray, String)
 */
public class JSONArray {

  /**
   * The arrayList where the JSONArray's properties are kept.
   */
  private final ArrayList<Object> myArrayList;


  /**
   * Construct an empty JSONArray.
   * @nowebref
   */
  public JSONArray() {
    this.myArrayList = new ArrayList<Object>();
  }


  /**
   * @nowebref
   */
  public JSONArray(Reader reader) {
    this(new JSONTokener(reader));
  }


  /**
   * Construct a JSONArray from a JSONTokener.
   * @param x A JSONTokener
   * @throws JSONException If there is a syntax error.
   */
  protected JSONArray(JSONTokener x) {
    this();
    if (x.nextClean() != '[') {
      throw new RuntimeException("A JSONArray text must start with '['");
    }
    if (x.nextClean() != ']') {
      x.back();
      for (;;) {
        if (x.nextClean() == ',') {
          x.back();
          myArrayList.add(JSONObject.NULL);
        } else {
          x.back();
          myArrayList.add(x.nextValue());
        }
        switch (x.nextClean()) {
        case ';':
        case ',':
          if (x.nextClean() == ']') {
            return;
          }
          x.back();
          break;
        case ']':
          return;
        default:
          throw new RuntimeException("Expected a ',' or ']'");
        }
      }
    }
  }


  /**
   * @nowebref
   */
  public JSONArray(IntList list) {
    myArrayList = new ArrayList<Object>();
    for (int item : list.values()) {
      myArrayList.add(new Integer(item));
    }
  }


  /**
   * @nowebref
   */
  public JSONArray(FloatList list) {
    myArrayList = new ArrayList<Object>();
    for (float item : list.values()) {
      myArrayList.add(new Float(item));
    }
  }


  /**
   * @nowebref
   */
  public JSONArray(StringList list) {
    myArrayList = new ArrayList<Object>();
    for (String item : list.values()) {
      myArrayList.add(item);
    }
  }


  /**
   * Construct a JSONArray from a source JSON text.
   * @param source     A string that begins with
   * <code>[</code>&nbsp;<small>(left bracket)</small>
   *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
   *  @throws JSONException If there is a syntax error.
   */
  static public JSONArray parse(String source) {
    try {
      return new JSONArray(new JSONTokener(source));
    } catch (Exception e) {
      return null;
    }
  }


//  /**
//   * Construct a JSONArray from a Collection.
//   * @param collection     A Collection.
//   */
//  public JSONArray(Collection collection) {
//    myArrayList = new ArrayList<Object>();
//    if (collection != null) {
//      Iterator iter = collection.iterator();
//      while (iter.hasNext()) {
//        myArrayList.add(JSONObject.wrap(iter.next()));
//      }
//    }
//  }


  // TODO not decided whether we keep this one, but used heavily by JSONObject
  /**
   * Construct a JSONArray from an array
   * @throws JSONException If not an array.
   */
  protected JSONArray(Object array) {
    this();
    if (array.getClass().isArray()) {
      int length = Array.getLength(array);
      for (int i = 0; i < length; i += 1) {
        this.append(JSONObject.wrap(Array.get(array, i)));
      }
    } else {
      throw new RuntimeException("JSONArray initial value should be a string or collection or array.");
    }
  }


  /**
   * Get the optional object value associated with an index.
   * @param index The index must be between 0 and length() - 1.
   * @return      An object value, or null if there is no
   *              object at that index.
   */
  private Object opt(int index) {
    if (index < 0 || index >= this.size()) {
      return null;
    }
    return myArrayList.get(index);
  }


  /**
   * Get the object value associated with an index.
   * @param index The index must be between 0 and length() - 1.
   * @return An object value.
   * @throws JSONException If there is no value for the index.
   */
  private Object get(int index) {
    Object object = opt(index);
    if (object == null) {
      throw new RuntimeException("JSONArray[" + index + "] not found.");
    }
    return object;
  }


  /**
   * Get the string associated with an index.
   * @param index The index must be between 0 and length() - 1.
   * @return      A string value.
   * @throws JSONException If there is no string value for the index.
   * @webref jsonarray:method
   * @brief Get the string associated with an index
   */
  public String getString(int index) {
    Object object = this.get(index);
    if (object instanceof String) {
      return (String)object;
    }
    throw new RuntimeException("JSONArray[" + index + "] not a string.");
  }


  /**
   * Get the int value associated with an index.
   *
   * @param index The index must be between 0 and length() - 1.
   * @return      The value.
   * @throws   JSONException If the key is not found or if the value is not a number.
   * @webref jsonarray:method
   * @brief Get the int value associated with an index
   */
  public int getInt(int index) {
    Object object = this.get(index);
    try {
      return object instanceof Number
        ? ((Number)object).intValue()
          : Integer.parseInt((String)object);
    } catch (Exception e) {
      throw new RuntimeException("JSONArray[" + index + "] is not a number.");
    }
  }


  /**
   * Get the long value associated with an index.
   *
   * @param index The index must be between 0 and length() - 1.
   * @return      The value.
   * @throws   JSONException If the key is not found or if the value cannot
   *  be converted to a number.
   */
  public long getLong(int index) {
    Object object = this.get(index);
    try {
      return object instanceof Number
        ? ((Number)object).longValue()
          : Long.parseLong((String)object);
    } catch (Exception e) {
      throw new RuntimeException("JSONArray[" + index + "] is not a number.");
    }
  }


  /**
   * Get a value from an index as a float. JSON uses 'double' values
   * internally, so this is simply getDouble() cast to a float.
   * 
   * @webref jsonarray:method
   * @brief To come...
   */
  public float getFloat(int index) {
    return (float) getDouble(index);
  }


  /**
   * Get the double value associated with an index.
   *
   * @param index The index must be between 0 and length() - 1.
   * @return      The value.
   * @throws   JSONException If the key is not found or if the value cannot
   *  be converted to a number.
   */
  public double getDouble(int index) {
    Object object = this.get(index);
    try {
      return object instanceof Number
        ? ((Number)object).doubleValue()
          : Double.parseDouble((String)object);
    } catch (Exception e) {
      throw new RuntimeException("JSONArray[" + index + "] is not a number.");
    }
  }


  /**
   * Get the boolean value associated with an index.
   * The string values "true" and "false" are converted to boolean.
   *
   * @param index The index must be between 0 and length() - 1.
   * @return      The truth.
   * @throws JSONException If there is no value for the index or if the
   *  value is not convertible to boolean.
   * @webref jsonarray:method
   * @brief Get the boolean value associated with an index
   */
  public boolean getBoolean(int index) {
    Object object = this.get(index);
    if (object.equals(Boolean.FALSE) ||
      (object instanceof String &&
        ((String)object).equalsIgnoreCase("false"))) {
      return false;
    } else if (object.equals(Boolean.TRUE) ||
      (object instanceof String &&
        ((String)object).equalsIgnoreCase("true"))) {
      return true;
    }
    throw new RuntimeException("JSONArray[" + index + "] is not a boolean.");
  }


  /**
   * Get the JSONArray associated with an index.
   * 
   * @param index The index must be between 0 and length() - 1.
   * @return      A JSONArray value.
   * @throws JSONException If there is no value for the index. or if the
   * value is not a JSONArray
   * @webref jsonarray:method
   * @brief Get the JSONArray associated with an index
   */
  public JSONArray getJSONArray(int index) {
    Object object = this.get(index);
    if (object instanceof JSONArray) {
      return (JSONArray)object;
    }
    throw new RuntimeException("JSONArray[" + index + "] is not a JSONArray.");
  }


  /**
   * Get the JSONObject associated with an index.
   * 
   * @param index subscript
   * @return      A JSONObject value.
   * @throws JSONException If there is no value for the index or if the
   * value is not a JSONObject
   * @webref jsonarray:method
   * @brief Get the JSONObject associated with an index
   */
  public JSONObject getJSONObject(int index) {
    Object object = this.get(index);
    if (object instanceof JSONObject) {
      return (JSONObject)object;
    }
    throw new RuntimeException("JSONArray[" + index + "] is not a JSONObject.");
  }


  /** 
   * Get this entire array as a String array. 
   * 
   * @webref jsonarray:method
   * @brief Get this entire array as a String array
   */
  public String[] getStringArray() {
    String[] outgoing = new String[size()];
    for (int i = 0; i < size(); i++) {
      outgoing[i] = getString(i);
    }
    return outgoing;
  }


  /** 
   * Get this entire array as an int array. Everything must be an int. 
   * 
   * @webref jsonarray:method
   * @brief Get this entire array as an int array
   */
  public int[] getIntArray() {
    int[] outgoing = new int[size()];
    for (int i = 0; i < size(); i++) {
      outgoing[i] = getInt(i);
    }
    return outgoing;
  }


  /** Get this entire array as a long array. Everything must be an long. */
  public long[] getLongArray() {
    long[] outgoing = new long[size()];
    for (int i = 0; i < size(); i++) {
      outgoing[i] = getLong(i);
    }
    return outgoing;
  }


  /** Get this entire array as a float array. Everything must be an float. */
  public float[] getFloatArray() {
    float[] outgoing = new float[size()];
    for (int i = 0; i < size(); i++) {
      outgoing[i] = getFloat(i);
    }
    return outgoing;
  }


  /** Get this entire array as a double array. Everything must be an double. */
  public double[] getDoubleArray() {
    double[] outgoing = new double[size()];
    for (int i = 0; i < size(); i++) {
      outgoing[i] = getDouble(i);
    }
    return outgoing;
  }


  /** Get this entire array as a boolean array. Everything must be a boolean. */
  public boolean[] getBooleanArray() {
    boolean[] outgoing = new boolean[size()];
    for (int i = 0; i < size(); i++) {
      outgoing[i] = getBoolean(i);
    }
    return outgoing;
  }


//  /**
//   * Get the optional boolean value associated with an index.
//   * It returns false if there is no value at that index,
//   * or if the value is not Boolean.TRUE or the String "true".
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @return      The truth.
//   */
//  public boolean optBoolean(int index)  {
//    return this.optBoolean(index, false);
//  }
//
//
//  /**
//   * Get the optional boolean value associated with an index.
//   * It returns the defaultValue if there is no value at that index or if
//   * it is not a Boolean or the String "true" or "false" (case insensitive).
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @param defaultValue     A boolean default.
//   * @return      The truth.
//   */
//  public boolean optBoolean(int index, boolean defaultValue)  {
//    try {
//      return this.getBoolean(index);
//    } catch (Exception e) {
//      return defaultValue;
//    }
//  }
//
//
//  /**
//   * Get the optional double value associated with an index.
//   * NaN is returned if there is no value for the index,
//   * or if the value is not a number and cannot be converted to a number.
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @return      The value.
//   */
//  public double optDouble(int index) {
//    return this.optDouble(index, Double.NaN);
//  }
//
//
//  /**
//   * Get the optional double value associated with an index.
//   * The defaultValue is returned if there is no value for the index,
//   * or if the value is not a number and cannot be converted to a number.
//   *
//   * @param index subscript
//   * @param defaultValue     The default value.
//   * @return      The value.
//   */
//  public double optDouble(int index, double defaultValue) {
//    try {
//      return this.getDouble(index);
//    } catch (Exception e) {
//      return defaultValue;
//    }
//  }
//
//
//  /**
//   * Get the optional int value associated with an index.
//   * Zero is returned if there is no value for the index,
//   * or if the value is not a number and cannot be converted to a number.
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @return      The value.
//   */
//  public int optInt(int index) {
//    return this.optInt(index, 0);
//  }
//
//
//  /**
//   * Get the optional int value associated with an index.
//   * The defaultValue is returned if there is no value for the index,
//   * or if the value is not a number and cannot be converted to a number.
//   * @param index The index must be between 0 and length() - 1.
//   * @param defaultValue     The default value.
//   * @return      The value.
//   */
//  public int optInt(int index, int defaultValue) {
//    try {
//      return this.getInt(index);
//    } catch (Exception e) {
//      return defaultValue;
//    }
//  }
//
//
//  /**
//   * Get the optional JSONArray associated with an index.
//   * @param index subscript
//   * @return      A JSONArray value, or null if the index has no value,
//   * or if the value is not a JSONArray.
//   */
//  public JSONArray optJSONArray(int index) {
//    Object o = this.opt(index);
//    return o instanceof JSONArray ? (JSONArray)o : null;
//  }
//
//
//  /**
//   * Get the optional JSONObject associated with an index.
//   * Null is returned if the key is not found, or null if the index has
//   * no value, or if the value is not a JSONObject.
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @return      A JSONObject value.
//   */
//  public JSON optJSONObject(int index) {
//    Object o = this.opt(index);
//    return o instanceof JSON ? (JSON)o : null;
//  }
//
//
//  /**
//   * Get the optional long value associated with an index.
//   * Zero is returned if there is no value for the index,
//   * or if the value is not a number and cannot be converted to a number.
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @return      The value.
//   */
//  public long optLong(int index) {
//    return this.optLong(index, 0);
//  }
//
//
//  /**
//   * Get the optional long value associated with an index.
//   * The defaultValue is returned if there is no value for the index,
//   * or if the value is not a number and cannot be converted to a number.
//   * @param index The index must be between 0 and length() - 1.
//   * @param defaultValue     The default value.
//   * @return      The value.
//   */
//  public long optLong(int index, long defaultValue) {
//    try {
//      return this.getLong(index);
//    } catch (Exception e) {
//      return defaultValue;
//    }
//  }
//
//
//  /**
//   * Get the optional string value associated with an index. It returns an
//   * empty string if there is no value at that index. If the value
//   * is not a string and is not null, then it is coverted to a string.
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @return      A String value.
//   */
//  public String optString(int index) {
//    return this.optString(index, "");
//  }
//
//
//  /**
//   * Get the optional string associated with an index.
//   * The defaultValue is returned if the key is not found.
//   *
//   * @param index The index must be between 0 and length() - 1.
//   * @param defaultValue     The default value.
//   * @return      A String value.
//   */
//  public String optString(int index, String defaultValue) {
//    Object object = this.opt(index);
//    return JSON.NULL.equals(object)
//      ? defaultValue
//        : object.toString();
//  }


  /**
   * Append an String value. This increases the array's length by one.
   *
   * @webref jsonarray:method
   * @brief Appends a String value, increasing the array's length by one
   * @param value A String value.
   * @return this.
   */
  public JSONArray append(String value) {
    this.append((Object)value);
    return this;
  }


  /**
   * Append an int value. This increases the array's length by one.
   *
   * @param value An int value.
   * @return this.
   */
  public JSONArray append(int value) {
    this.append(new Integer(value));
    return this;
  }


  /**
   * Append an long value. This increases the array's length by one.
   *
   * @param value A long value.
   * @return this.
   */
  public JSONArray append(long value) {
    this.append(new Long(value));
    return this;
  }


  /**
   * Append a float value. This increases the array's length by one.
   * This will store the value as a double, since there are no floats in JSON.
   *
   * @param value A float value.
   * @throws JSONException if the value is not finite.
   * @return this.
   */
  public JSONArray append(float value) {
    return append((double) value);
  }


  /**
   * Append a double value. This increases the array's length by one.
   *
   * @param value A double value.
   * @throws JSONException if the value is not finite.
   * @return this.
   */
  public JSONArray append(double value) {
    Double d = new Double(value);
    JSONObject.testValidity(d);
    this.append(d);
    return this;
  }


  /**
   * Append a boolean value. This increases the array's length by one.
   *
   * @param value A boolean value.
   * @return this.
   */
  public JSONArray append(boolean value) {
    this.append(value ? Boolean.TRUE : Boolean.FALSE);
    return this;
  }


//  /**
//   * Put a value in the JSONArray, where the value will be a
//   * JSONArray which is produced from a Collection.
//   * @param value A Collection value.
//   * @return      this.
//   */
//  public JSONArray append(Collection value) {
//    this.append(new JSONArray(value));
//    return this;
//  }


//  /**
//   * Put a value in the JSONArray, where the value will be a
//   * JSONObject which is produced from a Map.
//   * @param value A Map value.
//   * @return      this.
//   */
//  public JSONArray append(Map value) {
//    this.append(new JSONObject(value));
//    return this;
//  }


  public JSONArray append(JSONArray value) {
    myArrayList.add(value);
    return this;
  }


  public JSONArray append(JSONObject value) {
    myArrayList.add(value);
    return this;
  }


  /**
   * Append an object value. This increases the array's length by one.
   * @param value An object value.  The value should be a
   *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
   *  JSONObject.NULL object.
   * @return this.
   */
  protected JSONArray append(Object value) {
    myArrayList.add(value);
    return this;
  }


//  /**
//   * Put a value in the JSONArray, where the value will be a
//   * JSONArray which is produced from a Collection.
//   * @param index The subscript.
//   * @param value A Collection value.
//   * @return      this.
//   * @throws JSONException If the index is negative or if the value is
//   * not finite.
//   */
//  public JSONArray set(int index, Collection value) {
//    this.set(index, new JSONArray(value));
//    return this;
//  }


  /**
   * Put or replace a String value. If the index is greater than the length of
   *  the JSONArray, then null elements will be added as necessary to pad
   *  it out.
   * @param index The subscript.
   * @param value A String value.
   * @return this.
   * @throws JSONException If the index is negative.
   * @webref jsonarray:method
   * @brief Put or replace a String value
   */
  public JSONArray setString(int index, String value) {
    this.set(index, value);
    return this;
  }


  /**
   * Put or replace an int value. If the index is greater than the length of
   *  the JSONArray, then null elements will be added as necessary to pad
   *  it out.
   * @param index The subscript.
   * @param value An int value.
   * @return this.
   * @throws JSONException If the index is negative.
   * @webref jsonarray:method
   * @brief Put or replace an int value
   */
  public JSONArray setInt(int index, int value) {
    this.set(index, new Integer(value));
    return this;
  }


  /**
   * Put or replace a long value. If the index is greater than the length of
   *  the JSONArray, then null elements will be added as necessary to pad
   *  it out.
   * @param index The subscript.
   * @param value A long value.
   * @return this.
   * @throws JSONException If the index is negative.
   */
  public JSONArray setLong(int index, long value) {
    return set(index, new Long(value));
  }


  /**
   * Put or replace a float value. If the index is greater than the length
   * of the JSONArray, then null elements will be added as necessary to pad
   * it out. There are no 'double' values in JSON, so this is passed to
   * setDouble(value).
   * @param index The subscript.
   * @param value A float value.
   * @return this.
   * @throws RuntimeException If the index is negative or if the value is
   * not finite.
   * @webref jsonarray:method
   * @brief Put or replace a float value
   */
  public JSONArray setFloat(int index, float value) {
    return setDouble(index, value);
  }


  /**
   * Put or replace a double value. If the index is greater than the length of
   *  the JSONArray, then null elements will be added as necessary to pad
   *  it out.
   * @param index The subscript.
   * @param value A double value.
   * @return this.
   * @throws JSONException If the index is negative or if the value is
   * not finite.
   */
  public JSONArray setDouble(int index, double value) {
    return set(index, new Double(value));
  }


  /**
   * Put or replace a boolean value in the JSONArray. If the index is greater
   * than the length of the JSONArray, then null elements will be added as
   * necessary to pad it out.
   * @param index The subscript.
   * @param value A boolean value.
   * @return this.
   * @throws JSONException If the index is negative.
   * @webref jsonarray:method
   * @brief Put or replace a boolean value
   */
  public JSONArray setBoolean(int index, boolean value) {
    return set(index, value ? Boolean.TRUE : Boolean.FALSE);
  }


//  /**
//   * Put a value in the JSONArray, where the value will be a
//   * JSONObject that is produced from a Map.
//   * @param index The subscript.
//   * @param value The Map value.
//   * @return      this.
//   * @throws JSONException If the index is negative or if the the value is
//   *  an invalid number.
//   */
//  public JSONArray set(int index, Map value) {
//    this.set(index, new JSONObject(value));
//    return this;
//  }

  /**
   * @webref jsonarray:method
   * @brief To come...
   */
  public JSONArray setJSONArray(int index, JSONArray value) {
    set(index, value);
    return this;
  }

  /**
   * @webref jsonarray:method
   * @brief To come...
   */
  public JSONArray setJSONObject(int index, JSONObject value) {
    set(index, value);
    return this;
  }


  /**
   * Put or replace an object value in the JSONArray. If the index is greater
   *  than the length of the JSONArray, then null elements will be added as
   *  necessary to pad it out.
   * @param index The subscript.
   * @param value The value to put into the array. The value should be a
   *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
   *  JSONObject.NULL object.
   * @return this.
   * @throws JSONException If the index is negative or if the the value is
   *  an invalid number.
   */
  private JSONArray set(int index, Object value) {
    JSONObject.testValidity(value);
    if (index < 0) {
      throw new RuntimeException("JSONArray[" + index + "] not found.");
    }
    if (index < this.size()) {
      this.myArrayList.set(index, value);
    } else {
      while (index != this.size()) {
        this.append(JSONObject.NULL);
      }
      this.append(value);
    }
    return this;
  }


  /**
   * Get the number of elements in the JSONArray, included nulls.
   *
   * @webref jsonarray:method
   * @brief Gets the number of elements in the JSONArray
   * @return The length (or size).
   */
  public int size() {
    return myArrayList.size();
  }


  /**
   * Determine if the value is null.
   * @param index The index must be between 0 and length() - 1.
   * @return true if the value at the index is null, or if there is no value.
   */
  // TODO not sure on this one
  protected boolean isNull(int index) {
    return JSONObject.NULL.equals(this.opt(index));
  }


  /**
   * Remove an index and close the hole.
   * 
   * @webref jsonarray:method
   * @brief Removes an element
   * @param index the index value of the element to be removed
   * @return The value that was associated with the index, or null if there was no value.
   */
  public Object remove(int index) {
    Object o = this.opt(index);
    this.myArrayList.remove(index);
    return o;
  }


//  /**
//   * Produce a JSONObject by combining a JSONArray of names with the values
//   * of this JSONArray.
//   * @param names A JSONArray containing a list of key strings. These will be
//   * paired with the values.
//   * @return A JSONObject, or null if there are no names or if this JSONArray
//   * has no values.
//   * @throws JSONException If any of the names are null.
//   */
//  public JSON toJSONObject(JSONArray names) {
//    if (names == null || names.length() == 0 || this.length() == 0) {
//      return null;
//    }
//    JSON jo = new JSON();
//    for (int i = 0; i < names.length(); i += 1) {
//      jo.put(names.getString(i), this.opt(i));
//    }
//    return jo;
//  }


  protected boolean save(OutputStream output) {
    return save(PApplet.createWriter(output));
  }


  public boolean save(File file, String options) {
    return save(PApplet.createWriter(file));
  }


  public boolean save(PrintWriter output) {
    output.print(format(2));
    output.flush();
    return true;
  }


  /**
   * Return the JSON data formatted with two spaces for indents.
   * Chosen to do this since it's the most common case (e.g. with println()).
   * Same as format(2). Use the format() function for more options.
   */
  @Override
  public String toString() {
    try {
      return format(2);
    } catch (Exception e) {
      return null;
    }
  }


  /**
   * Make a pretty-printed JSON text of this JSONArray.
   * Warning: This method assumes that the data structure is acyclical.
   * @param indentFactor The number of spaces to add to each level of
   *  indentation. Use -1 to specify no indentation and no newlines.
   * @return a printable, displayable, transmittable
   *  representation of the object, beginning
   *  with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
   *  with <code>]</code>&nbsp;<small>(right bracket)</small>.
   */
  public String format(int indentFactor) {
    StringWriter sw = new StringWriter();
    synchronized (sw.getBuffer()) {
      return this.write(sw, indentFactor, 0).toString();
    }
  }

  /**
   * Write the contents of the JSONArray as JSON text to a writer. For
   * compactness, no whitespace is added.
   * <p>
   * Warning: This method assumes that the data structure is acyclic.
   *
   * @return The writer.
   */
  protected Writer write(Writer writer) {
    return this.write(writer, -1, 0);
  }

  /**
   * Write the contents of the JSONArray as JSON text to a writer. For
   * compactness, no whitespace is added.
   * <p>
   * Warning: This method assumes that the data structure is acyclic.
   *
   * @param indentFactor
   *            The number of spaces to add to each level of indentation.
   *            Use -1 to specify no indentation and no newlines.
   * @param indent
   *            The indention of the top level.
   * @return The writer.
   * @throws JSONException
   */
  protected Writer write(Writer writer, int indentFactor, int indent) {
    try {
      boolean commanate = false;
      int length = this.size();
      writer.write('[');

      // Use -1 to signify 'no indent'
      int thisFactor = (indentFactor == -1) ? 0 : indentFactor;

      if (length == 1) {
        JSONObject.writeValue(writer, this.myArrayList.get(0),
                              thisFactor, indent);
      } else if (length != 0) {
        final int newindent = indent + thisFactor;

        for (int i = 0; i < length; i += 1) {
          if (commanate) {
            writer.write(',');
          }
          if (indentFactor != -1) {
            writer.write('\n');
          }
          JSONObject.indent(writer, newindent);
          JSONObject.writeValue(writer, this.myArrayList.get(i),
                                thisFactor, newindent);
          commanate = true;
        }
        if (indentFactor != -1) {
          writer.write('\n');
        }
        JSONObject.indent(writer, indent);
      }
      writer.write(']');
      return writer;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Make a string from the contents of this JSONArray. The
   * <code>separator</code> string is inserted between each element.
   * Warning: This method assumes that the data structure is acyclic.
   * @param separator A string that will be inserted between the elements.
   * @return a string.
   * @throws JSONException If the array contains an invalid number.
   */
  public String join(String separator) {
    int len = this.size();
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < len; i += 1) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
    }
    return sb.toString();
  }
}
