/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage.elementlocked;

import com.maxifier.mxcache.storage.*;

/**
 * DoubleByteElementLockedStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PStorage.template
 *
 * Относительно абстрактных методов этого класса даются следующие гарантии:
 * 1) Методы contains, get и put всегда вызываются с блокировкой.
 * 2) Если реализация использует read-write блокировку, то методы contains и get
 *    вызываются с блокировкой на чтение, а put - с блокировкой на запись.
 * 3) Если метод contains возвращает true, то гарантируется, что вскоре при
 *    удерживаемой блокировке будет вызван метод get с таким же аргументом.
 *    (для метода put таких гарантий не дается!)
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface DoubleByteElementLockedStorage extends DoubleByteStorage, ElementLockedStorage {
    void lock(double key);

    void unlock(double key);
}