package ch.njol.yggdrasil;

import ch.njol.yggdrasil.Fields.FieldContext;

import java.io.StreamCorruptedException;
import java.lang.reflect.Field;

public interface FieldHandler {
	
	/**
	 * Called when a loaded field doesn't exist.
	 * 
	 * @param object The object whose filed is missing
	 * @param field The field read from stream
	 * @return Whether this Handler handled the request
	 */
	boolean excessiveField(Object object, FieldContext field) throws StreamCorruptedException;
	
	/**
	 * Called if a field was not found in the stream.
	 * 
	 * @param object The object whose filed is missing
	 * @param field The field that didn't occur in the stream
	 * @return Whether this Handler handled the request
	 */
	boolean missingField(Object object, Field field) throws StreamCorruptedException;
	
	/**
	 * Called when a loaded value is not compatible with the type of field.
	 * 
	 * @param object The object the field belongs to
	 * @param field The field to set
	 * @param context The field read from stream
	 * @return Whether this Handler handled the request
	 */
	boolean incompatibleField(Object object, Field field, FieldContext context) throws StreamCorruptedException;
	
}
