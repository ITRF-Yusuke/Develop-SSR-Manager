/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData

import org.kijitora.develop.ssr.db.AppDatabase
import org.kijitora.develop.ssr.db.dataclass.entity.Account
import org.kijitora.develop.ssr.db.dataclass.UserUnitWithMaster
import org.kijitora.develop.ssr.db.dataclass.entity.UserUnit
import org.kijitora.develop.ssr.db.dataclass.entity.MasterUnit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ユニット一覧画面のための ViewModel クラスです。
 *
 * このクラスはアプリのデータベース（`AppDatabase`）を使用して、
 * ユーザーのユニットデータの取得、更新、管理を担当します。
 * ViewModel は UI コンポーネントのライフサイクルに依存せず、
 * データの保持と非同期処理を安定的に行います。
 *
 * @property appDatabase アプリケーションのデータベースインスタンス
 */
class UnitListViewModel(private val appDatabase: AppDatabase) : ViewModel() {

    // 監視可能なデータホルダー
    // つまりアカウントIDを監視している
    private val _currentAccountId = MutableLiveData<Long?>(null)
    val currentAccountId: LiveData<Long?> = _currentAccountId

    val accounts: LiveData<List<Account>> = appDatabase.accountDao().getAll().asLiveData()

    private val _searchText = MutableLiveData<String>("")
    val searchText: LiveData<String> = _searchText

    private val _refreshTrigger = MutableLiveData<Unit>(Unit)

    // アカウントIDが変更された時、または検索文字が変更された時にライブデータを更新する
    val userUnitsWithMaster: LiveData<List<UserUnitWithMaster>> =
        MediatorLiveData<List<UserUnitWithMaster>>().apply {
            fun load(accountId: Long?, query: String?) {
                if (accountId != null) {
                    viewModelScope.launch {
                        val result = appDatabase.userUnitDao()
                            .searchUserUnitsByAccountId(accountId, query ?: "")
                            .firstOrNull() ?: emptyList()
                        value = result
                    }
                } else {
                    value = emptyList()
                }
            }

            addSource(_currentAccountId) { load(it, _searchText.value) }
            addSource(_searchText) { load(_currentAccountId.value, it) }
            addSource(_refreshTrigger) { load(_currentAccountId.value, _searchText.value) }
        }

    fun refreshData() {
        _refreshTrigger.value = Unit
    }

    /**
     * 今選択しているアカウントを設定します.
     *
     * @param accountId アカウントID
     */
    fun setCurrentAccount(accountId: Long) {
        _currentAccountId.value = accountId
    }

    /**
     * 検索ワードを設定します.
     *
     * @param searchText 検索ワード
     */
    fun setSearchText(searchText: String) {
        _searchText.value = searchText
    }

    /**
     * 新しいアカウントを追加します.
     *
     * @param accountName アカウント名
     */
    fun addAccount(accountName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // アカウント名を登録して、自動採番されたアカウントIDを取得する
            val accountId = appDatabase.accountDao().insert(Account(accountName = accountName))

            if (accountId > 0) {
                appDatabase.masterUnitDao().getAllMasterUnits().flowOn(Dispatchers.IO)
                    .collect { masterUnits ->
                        val initialUserUnits = masterUnits.map { masterUnit ->
                            UserUnit(accountId = accountId, unitName = masterUnit.unitName)
                        }
                        appDatabase.userUnitDao().insertAll(initialUserUnits)
                    }
            }

        }
    }

    /**
     * アカウントを削除します.
     *
     * @param accountId アカウントID
     */
    fun delAccount(accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.userUnitDao().delete(accountId)
            appDatabase.accountDao().delete(accountId)
        }
        _currentAccountId.value = 0
    }

    /**
     * 指定されたユーザーユニットのデータをデータベースに保存します。
     *
     * この関数は ViewModel のスコープで IOスレッド上に非同期で処理を行い、
     * `insertOrUpdate` を使用してデータベースに新規挿入または更新を行います。
     *
     * @param userUnit 保存対象のユーザーユニットデータ
     */
    fun updateUserUnit(userUnit: UserUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.userUnitDao().insertOrUpdate(userUnit)
        }
    }

    /**
     * マスターユニット情報をデータベースに登録し、対応するユーザーユニットを初期化します。
     *
     * 指定されたマスターユニットリストをデータベースに一括挿入し、
     * その後、全マスターユニットを取得して、各ユニットに対して
     * 指定されたアカウント ID に紐づく `UserUnit` を生成・挿入します。
     * `UserUnit` の挿入時には既存のデータと競合した場合は無視されます。
     *
     * この処理は ViewModel のスコープ内で非同期（IO スレッド）に実行されます。
     *
     * @param units データベースに登録するマスターユニットの一覧
     * @param accountId 対応するユーザーユニットに紐づけるアカウント ID
     */
    fun updateMasterUnits(units: List<MasterUnit>, accountId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.masterUnitDao().insertAll(units)

            appDatabase.masterUnitDao().getAllMasterUnits().flowOn(Dispatchers.IO)
                .collect { masterUnits ->
                    val initialUserUnits = masterUnits.map { masterUnit ->
                        UserUnit(accountId = accountId, unitName = masterUnit.unitName)
                    }
                    appDatabase.userUnitDao().insertAllIgnoreWhenConflict(initialUserUnits)
                }
        }

    }

    /**
     * 初期データとしてマスターユニット一覧をデータベースに挿入します。
     *
     * 渡されたマスターユニットのリストをデータベースに一括で登録します。
     * この処理は ViewModel のスコープ内で IO スレッド上に非同期で実行されます。
     * 主にアプリ初回起動時やデータ初期化時に使用される想定です。
     *
     * @param units 挿入するマスターユニットのリスト
     */
    fun insertInitialMasterUnits(units: List<MasterUnit>) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.masterUnitDao().insertAll(units)
        }
    }

}