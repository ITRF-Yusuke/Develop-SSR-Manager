/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import org.kijitora.develop.ssr.viewmodel.UnitListViewModel
import org.kijitora.develop.ssr.viewmodel.UnitListViewModelFactory

/**
 * アプリのメイン画面を構成するアクティビティです。
 *
 * このアクティビティはアプリの起動時に最初に表示され、
 * ユーザーインターフェースの初期化や、必要なデータの読み込み、
 * フラグメントの表示などを行います。
 *
 * `AppCompatActivity` を継承しており、互換性のあるアクションバーや
 * ライフサイクル管理機能を提供します。
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: UnitListViewModel by viewModels {
        UnitListViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}