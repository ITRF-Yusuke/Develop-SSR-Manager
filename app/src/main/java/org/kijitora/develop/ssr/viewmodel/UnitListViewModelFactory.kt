/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import org.kijitora.develop.ssr.db.AppDatabase

/**
 * 機体一覧のViewModelを生成するクラス.
 */
class UnitListViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {

    /**
     * 指定された ViewModel クラスのインスタンスを生成して返します。
     *
     * このファクトリーメソッドは `UnitListViewModel` のみをサポートしており、
     * 他の ViewModel クラスが要求された場合は例外をスローします。
     * ViewModel にはアプリケーションスコープのデータベースインスタンスが渡されます。
     *
     * @param T 生成する ViewModel の型
     * @param modelClass 要求された ViewModel クラスの `Class` オブジェクト
     * @return 要求された型の ViewModel インスタンス
     * @throws IllegalArgumentException 未対応の ViewModel クラスが要求された場合
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UnitListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UnitListViewModel(AppDatabase.getDatabase(application)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}