package com.maxifier.mxcache.idea;

import com.maxifier.mxcache.Cached;

import javax.xml.bind.JAXBException;

public class IdeaJaxb {

    private int get() throws JAXBException {
        return 1;
    }

    @Cached
    public int cached()  {
        try {
            return get();
        } catch (RuntimeException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
