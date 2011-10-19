package com.maxifier.mxcache.storage.elementlocked;

import com.maxifier.mxcache.storage.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:54:51
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 * <p>
 * ќтносительно абстрактных методов этого класса даютс€ следующие гарантии:
 * 1) ћетоды contains, get и put всегда вызываютс€ с блокировкой.
 * 2) ≈сли реализаци€ использует read-write блокировку, то методы contains и get
 *    вызываютс€ с блокировкой на чтение, а put - с блокировкой на запись.
 * 3) ≈сли метод contains возвращает true, то гарантируетс€, что вскоре при
 *    удерживаемой блокировке будет вызван метод get с таким же аргументом.
 *    (дл€ метода put таких гарантий не даетс€!)
 *
 * @author ELectronic ENgine
 */
public interface CharacterFloatElementLockedStorage extends CharacterFloatStorage, ElementLockedStorage {
    void lock(char key);

    void unlock(char key);
}