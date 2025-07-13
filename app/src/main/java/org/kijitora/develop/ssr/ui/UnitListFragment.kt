/*
 * Copyright 2025 ITRF.
 */
package org.kijitora.develop.ssr.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.kijitora.develop.ssr.R
import org.kijitora.develop.ssr.db.dataclass.entity.MasterUnit
import org.kijitora.develop.ssr.http.JsonDownloader
import org.kijitora.develop.ssr.util.FileUtils
import org.kijitora.develop.ssr.viewmodel.UnitListViewModel
import org.kijitora.develop.ssr.viewmodel.UnitListViewModelFactory

/**
 * 機体一覧のフラグメント.
 */
class UnitListFragment : Fragment() {

    private val viewModel: UnitListViewModel by viewModels {
        UnitListViewModelFactory(requireActivity().application)
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var accountSpinner: Spinner
    private lateinit var searchEditText: EditText
    private lateinit var unitRecyclerView: RecyclerView
    private lateinit var unitListAdapter: UnitListAdapter

    private var currentAccountId: Long = 0

    /**
     * フラグメントのレイアウトを生成して返します。
     *
     * このメソッドは、フラグメントが初めて表示されるときに呼び出され、
     * `fragment_unit_list.xml` のレイアウトをインフレートして画面に表示します。
     *
     * @param inflater レイアウト XML を View オブジェクトに変換するための LayoutInflater
     * @param container このフラグメントの UI を配置する親ビュー（通常は ViewGroup）
     * @param savedInstanceState フラグメントの以前の状態が保存されている Bundle（null の場合あり）
     * @return フラグメントのルート View を返します（ここでは fragment_unit_list.xml）
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unit_list, container, false)
    }

    /**
     * フラグメントのビューが作成された後に呼び出されるコールバックです。
     *
     * このメソッドは、`onCreateView` によって生成された `View` が完全に作成された直後に呼ばれ、
     * ボタンやリストビューなどの UI コンポーネントの初期化や、イベントリスナーの設定などを行うのに適しています。
     *
     * @param view フラグメントのルートビュー
     * @param savedInstanceState 前回保存された状態が含まれる Bundle（null の場合もあります）
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)

        accountSpinner = view.findViewById(R.id.accountSpinner)
        searchEditText = view.findViewById(R.id.searchEditText)
        // 機体リサイクルビュー
        unitRecyclerView = view.findViewById(R.id.unitRecyclerView)
        unitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // 機体リストアダプター
        unitListAdapter = UnitListAdapter(viewModel) // AdapterにViewModelを渡す（必要に応じて）
        unitRecyclerView.adapter = unitListAdapter

        // アカウントリストの監視
        viewModel.accounts.observe(viewLifecycleOwner, Observer { accounts ->
            val accountNames = accounts.map { it.accountName }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                accountNames
            )
            accountSpinner.adapter = adapter

            // アカウントが選択された時の処理
            accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (accounts.isNotEmpty()) {
                        viewModel.setCurrentAccount(accounts[position].accountId)
                        currentAccountId = accounts[position].accountId
                        searchEditText.visibility = View.VISIBLE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    viewModel.setCurrentAccount(0)
                    searchEditText.visibility = View.INVISIBLE
                }
            }
            // 初期アカウントが設定されている場合は選択
            if (accounts.isNotEmpty()) {
                // 例：最初のアカウントを初期選択
                viewModel.setCurrentAccount(accounts[0].accountId)
                accountSpinner.setSelection(0)
                searchEditText.visibility = View.VISIBLE

            }
        })

        // 検索テキストの監視
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchText(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // ユーザーの機体リストの監視
        viewModel.userUnitsWithMaster.observe(viewLifecycleOwner, Observer { units ->
            unitListAdapter.submitList(units)
        })

        // 新しいアカウントを追加するボタンなどの処理（例）
        val addAccountButton = view.findViewById<Button>(R.id.addAccountButton)
        addAccountButton?.setOnClickListener {
            // アラートダイアログなどでアカウント名を入力させる
            val editText = EditText(requireContext())
            editText.setSingleLine(true)
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.message_input_account_name)
                .setView(editText)
                .setPositiveButton(R.string.button_add) { _, _ ->
                    val accountName = editText.text.toString().trim()
                    if (accountName.isNotEmpty()) {
                        viewModel.addAccount(accountName)
                    }
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
        }

        initMasterData()

        // 新しいアカウントを追加するボタンなどの処理（例）
        val delAccountButton = view.findViewById<Button>(R.id.delAccountButton)
        delAccountButton?.setOnClickListener {
            // 削除確認ダイアログを表示する
            showUpdateMasterDataDialog()

        }


        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        showDeleteConfirmDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

    }

    /**
     * マスターデータを初期化します.
     */
    private fun initMasterData() {

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isInitialized = prefs.getBoolean("is_db_initialized", false)

        if (isInitialized) {
            return
        }

        // 初期マスターデータの投入（初回起動時などに行う）
        val masterUnitList: List<MasterUnit>? = FileUtils.loadJsonFromRaw<List<MasterUnit>>(requireContext(), R.raw.initial_unit_data)

        if (masterUnitList != null) {
            viewModel.insertInitialMasterUnits(masterUnitList)
        }

        // 状態を保存
        prefs.edit().putBoolean("is_db_initialized", true).apply()
    }

    /**
     * マスターデータ更新ダイアログを表示します.
     */
    private fun showUpdateMasterDataDialog() {
        val dialogView = layoutInflater.inflate(R.layout.master_data_update_dialog, null)
        val editText = dialogView.findViewById<EditText>(R.id.master_data_json_edittext)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(R.string.title_unit_data_update)
            .setMessage(R.string.message_confirm_get_master_data)
            .setNeutralButton(R.string.button_get_data_from_server, null)
            .setNegativeButton(R.string.button_apply) { _, _ ->

                val masterUnitListType = object : TypeToken<List<MasterUnit>>() {}.type
                val masterUnitList: List<MasterUnit> = Gson().fromJson(editText.text.toString(), masterUnitListType)
                viewModel.updateMasterUnits(masterUnitList, currentAccountId)

                viewModel.refreshData()
            }
            .setPositiveButton(R.string.button_close, null)
            .create()
        dialog.setOnShowListener {
            val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                Toast.makeText(requireContext(), R.string.message_getting_from_server, Toast.LENGTH_SHORT).show()
                JsonDownloader.getMasterData(requireContext()) { jsonArray:String?  ->
                    requireActivity().runOnUiThread {
                        editText.setText(jsonArray)
                    }

                }
            }
        }

        dialog.show()
    }

    /**
     * 削除確認ダイアログを表示します.
     */
    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_account_deletion_confirmation)
            .setMessage(R.string.message_confirm_delete_account)
            .setPositiveButton(R.string.button_ok) { _, _ ->

                // アカウント削除
                viewModel.delAccount(currentAccountId)

            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

}
