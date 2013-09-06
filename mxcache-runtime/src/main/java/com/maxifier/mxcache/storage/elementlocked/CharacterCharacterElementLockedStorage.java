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
 * Относительно абстрактных методов этого класса даются следующие гарантии:
 * 1) Методы contains, get и put всегда вызываются с блокировкой.
 * 2) Если реализация использует read-write блокировку, то методы contains и get
 *    вызываются с блокировкой на чтение, а put - с блокировкой на запись.
 * 3) Если метод contains возвращает true, то гарантируется, что вскоре при
 *    удерживаемой блокировке будет вызван метод get с таким же аргументом.
 *    (для метода put таких гарантий не дается!)
 *
 * @author ELectronic ENgine
 */
public interface CharacterCharacterElementLockedStorage extends CharacterCharacterStorage, ElementLockedStorage {
    void lock(char key);

    void unlock(char key);
}