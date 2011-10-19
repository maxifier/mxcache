package com.maxifier.mxcache.legacy.converters;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.03.2009
 * Time: 14:46:08
 */
public interface MxConvertType {

    int getId();

    MxConvertType DEFAULT = new MxConvertType() {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String toString() {
            return "NONE";
        }
    };

    MxConvertType[] ONLY_DEFAULT = { DEFAULT };
}
