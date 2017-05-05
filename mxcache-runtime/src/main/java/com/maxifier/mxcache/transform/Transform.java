/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Use this annotation on @Cached method arguments to make the form of the key stored in cache different from the
 * original object.
 * </p><p>
 * Transform annotations are "infectious": you can annotate another annotation with @Transform, and MxCache will
 * recognize that annotation to have exactly the same meaning as original transform. <b>Don't forget to put
 * {@code @Retention(RetentionPolicy.RUNTIME)} on your third-party transform annotations!</b></p>
 *
 * For more defaults see <a href="https://github.com/maxifier/mxcache/wiki/Key-Transformation">Key Transformation</a>.
 *
 * @see com.maxifier.mxcache.transform.CustomTransformAnnotation
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transform {
    String ONLY_PUBLIC_METHOD = "<the only public method>";

    /**
     * @return owner class of transform method. By default MxCache will invoke a method of key itself.
     *     Otherwise it will lookup an instance of transformer with {@link com.maxifier.mxcache.InstanceProvider}.
     */
    Class owner() default KEY_ITSELF.class;

    /**
     * @return method name. By default MxCache will lookup for the only public method of owner.
     *     Transform method should always have a return type.
     *     If owner is key itself, the method should have no arguments.
     *     Otherwise transform method should have exactly one argument.
     */
    String method() default ONLY_PUBLIC_METHOD;

    final class KEY_ITSELF {
        private KEY_ITSELF() {
        }
    }
}
