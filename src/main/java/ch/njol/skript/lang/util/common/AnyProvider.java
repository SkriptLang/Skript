package ch.njol.skript.lang.util.common;

/**
 * 'AnyProvider' types are holders for common properties (e.g. name, size) where
 * it is highly likely that things other than Skript may wish to register
 * exponents of the property.
 * <br/>
 * <br/>
 * If possible, types should implement an {@link AnyProvider} subtype directly for
 * the best possible parsing efficiency.
 * However, implementing the interface may not be possible if:
 * <ul>
 *     <li>registering an existing class from a third-party library</li>
 *     <li>the subtype getter method conflicts with the type's own methods
 *     or erasure</li>
 *     <li>the presence of the supertype might confuse the class's design</li>
 * </ul>
 * In these cases, a converter from the class to the AnyX type can be registered.
 * The converter should not permit right-chaining or unsafe casts.
 * <br/>
 * <br/>
 * The root provider supertype cannot include its own common methods, since these
 * may conflict between things that provide two values (e.g. something declaring
 * both a name and a size)
 */
public interface AnyProvider {

}
