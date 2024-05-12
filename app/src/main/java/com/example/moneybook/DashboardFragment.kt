package com.example.moneybook

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneybook.Model.Data
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.util.Date

class DashboardFragment : Fragment() {

    //Floating button
    private lateinit var fab_main_btn: FloatingActionButton
    private lateinit var fab_income_btn: FloatingActionButton
    private lateinit var fab_expense_btn: FloatingActionButton

    //TextView dei floating button
    private lateinit var fab_income_text: TextView
    private lateinit var fab_expense_text: TextView

    private var isOpen: Boolean = false

    //Animation
    private lateinit var fadeOpen: Animation
    private lateinit var fadeClose: Animation

    //Aggiornamento risultato totale dashboard entrate/uscite
    private lateinit var totalIncomeResult: TextView
    private lateinit var totalExpenseResult: TextView

    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var incomeDatabase: DatabaseReference
    private lateinit var expenseDatabase: DatabaseReference

    //Recycler View Income/Expense Dashboard
    private lateinit var recyclerIncome: RecyclerView
    private lateinit var recyclerExpense: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_dashboard, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val user: FirebaseUser = firebaseAuth.currentUser!!
        val uid: String = user.uid

        //Firebase di default usa getInstance posizionato in us-central ,se db è posizionato in europa va aggiunto il link a mano per dare la posizione di ricerca corretta
        incomeDatabase = FirebaseDatabase.getInstance("https://moneybook-f9f3a-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("IncomeData").child(uid)
        expenseDatabase = FirebaseDatabase.getInstance("https://moneybook-f9f3a-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("ExpenseDatabase").child(uid)

        //connessione floating button con il layout
        fab_main_btn = myView.findViewById(R.id.fb_main_plus_btn)
        fab_income_btn = myView.findViewById(R.id.income_ft_btn)
        fab_expense_btn = myView.findViewById(R.id.expense_ft_btn)

        //Connessione testo floating button con il layout
        fab_income_text = myView.findViewById(R.id.income_ft_text)
        fab_expense_text = myView.findViewById(R.id.expense_ft_text)

        //connessione TextView totale income/expense
        totalIncomeResult = myView.findViewById(R.id.income_set_result)
        totalExpenseResult = myView.findViewById(R.id.expense_set_result)

        //Connessione Animation con layout
        fadeOpen = AnimationUtils.loadAnimation(activity,R.anim.fade_open)
        fadeClose = AnimationUtils.loadAnimation(activity,R.anim.fade_close)

        //Connessione delle recycler view
        recyclerIncome = myView.findViewById(R.id.recycler_income)
        recyclerExpense = myView.findViewById(R.id.recycler_expense)

        fab_main_btn.setOnClickListener {

            addData()

            floatingButtonAnimation()
        }

        //Calcolo totale Entrate
        incomeDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sommaTotaleEntrate = 0
                for (mySnapshot in dataSnapshot.children){
                    val data = mySnapshot.getValue(Data::class.java)
                    sommaTotaleEntrate+=data?.amount?:0
                }
                val strSommaTot = sommaTotaleEntrate.toString()
                totalIncomeResult.text = "$strSommaTot.00"

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //Calcolo delle Uscite
        expenseDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sommaTotaleUscite = 0
                for (mySnapshot in dataSnapshot.children){
                    val data = mySnapshot.getValue(Data::class.java)
                    sommaTotaleUscite+=data?.amount?:0
                }
                val strSommaTot = sommaTotaleUscite.toString()
                totalExpenseResult.text = "$strSommaTot.00"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        //Inizializzazione RecyclerView
        var layoutManagerIncome = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
        layoutManagerIncome.stackFromEnd = true
        layoutManagerIncome.reverseLayout = true
        recyclerIncome.setHasFixedSize(true)
        recyclerIncome.layoutManager = layoutManagerIncome

        var layoutManagerExpense = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
        layoutManagerExpense.stackFromEnd = true
        layoutManagerExpense.reverseLayout = true
        recyclerExpense.setHasFixedSize(true)
        recyclerExpense.layoutManager = layoutManagerExpense

        return myView
    }

    //Caricamento delle entrate Uscite nelle recyclerView al load del DashboardFragment

    override fun onStart() {
        super.onStart()

        val options: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(incomeDatabase, Data::class.java)
            .build()

        val incomeAdapter = object: FirebaseRecyclerAdapter<Data,IncomeViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboarad_income, parent,false)
                return IncomeViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: IncomeViewHolder, position: Int, model: Data) {
                viewHolder.setIncomeType(model.type)
                viewHolder.setIncomeAmount(model.amount)
                viewHolder.setIncomeDate(model.date)
            }
        }

        recyclerIncome.adapter = incomeAdapter
        incomeAdapter.startListening()

    }

    inner class IncomeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var myIncomeView: View = itemView

        fun setIncomeType(type: String){
            var mType: TextView = myIncomeView.findViewById(R.id.type_income_ds)
            mType.text = type
        }

        fun setIncomeAmount(amount: Int){
            var mAmount: TextView = myIncomeView.findViewById(R.id.amount_income_ds)
            var strAmount = amount.toString()
            mAmount.text = strAmount
        }

        fun setIncomeDate(date: String){
            var mDate: TextView = myIncomeView.findViewById(R.id.date_income_ds)
            mDate.text = date
        }
    }

    //Floating button animation
    fun floatingButtonAnimation(){
        if(isOpen){
            fab_income_btn.startAnimation(fadeClose)
            fab_expense_btn.startAnimation(fadeClose)
            fab_income_btn.isClickable = false
            fab_expense_btn.isClickable = false

            fab_income_text.startAnimation(fadeClose)
            fab_expense_text.startAnimation(fadeClose)
            fab_income_text.isClickable = false
            fab_expense_text.isClickable = false
            isOpen = false
        }else{
            fab_income_btn.startAnimation(fadeOpen)
            fab_expense_btn.startAnimation(fadeOpen)
            fab_income_btn.isClickable = true
            fab_expense_btn.isClickable = true

            fab_income_text.startAnimation(fadeOpen)
            fab_expense_text.startAnimation(fadeOpen)
            fab_income_text.isClickable = true
            fab_expense_text.isClickable = true
            isOpen = true
        }
    }

    private fun addData(){
        //Floating button income:

        fab_income_btn.setOnClickListener {
            incomeDataInsert()
        }

        fab_expense_btn.setOnClickListener {
            expenseDataInsert()
        }
    }

    fun incomeDataInsert(){
        val myDialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myView = inflater.inflate(R.layout.custom_layout_for_insert_data,null)
        myDialog.setView(myView)
        val dialog = myDialog.create()
        //setCancelable(false) evita che i click al di fuori del dialog vengano presi in considerazione
        dialog.setCancelable(false)

        val edittextAmount: EditText = myView.findViewById(R.id.amount_edittext)
        val edittextType: EditText = myView.findViewById(R.id.type_edittext)
        val edittextNote: EditText = myView.findViewById(R.id.note_edittext)

        val btnSave: Button = myView.findViewById(R.id.btnSave)
        val btnCancel: Button = myView.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            val type: String = edittextType.text.toString().trim()
            val amount: String = edittextAmount.text.toString().trim()
            val note: String = edittextNote.text.toString().trim()

            if(type.isEmpty()){
                edittextType.error = "Campo richiesto"
                return@setOnClickListener
            }
            if(amount.isEmpty()){
                edittextAmount.error = "Campo richiesto"
                return@setOnClickListener
            }

            val ourAmountInt: Int = amount.toInt()

            if(note.isEmpty()){
                edittextNote.error = "Campo richiesto"
                return@setOnClickListener
            }

            //codice per aggiungere al db una Entrata
            val id: String = incomeDatabase.push().key!!
            val date: String = DateFormat.getDateInstance().format(Date())
            val data = Data(ourAmountInt,type,note,id, date)
            incomeDatabase.child(id).setValue(data)
            Toast.makeText(activity,"Dati inseriti",Toast.LENGTH_SHORT).show()

            floatingButtonAnimation()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            floatingButtonAnimation()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun expenseDataInsert(){
        val myDialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val myView = inflater.inflate(R.layout.custom_layout_for_insert_data,null)
        myDialog.setView(myView)
        val dialog = myDialog.create()
        dialog.setCancelable(false)

        val edittextAmount: EditText = myView.findViewById(R.id.amount_edittext)
        val edittextType: EditText = myView.findViewById(R.id.type_edittext)
        val edittextNote: EditText = myView.findViewById(R.id.note_edittext)

        val btnSave: Button = myView.findViewById(R.id.btnSave)
        val btnCancel: Button = myView.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            val type: String = edittextType.text.toString().trim()
            val amount: String = edittextAmount.text.toString().trim()
            val note: String = edittextNote.text.toString().trim()

            if(type.isEmpty()){
                edittextType.error = "Campo richiesto"
                return@setOnClickListener
            }
            if(amount.isEmpty()){
                edittextAmount.error = "Campo richiesto"
                return@setOnClickListener
            }
            val inAmountInt: Int = amount.toInt()

            if(note.isEmpty()){
                edittextNote.error = "Campo richiesto"
                return@setOnClickListener
            }

            //codice per aggiungere al db una Uscita
            val id: String = incomeDatabase.push().key!!
            val date: String = DateFormat.getDateInstance().format(Date())
            val data = Data(inAmountInt,type,note,id, date)
            expenseDatabase.child(id).setValue(data)
            Toast.makeText(activity,"Dati inseriti",Toast.LENGTH_SHORT).show()

            floatingButtonAnimation()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            floatingButtonAnimation()
            dialog.dismiss()
        }

        dialog.show()

    }

}