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
import com.example.moneybook.Model.Data
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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

    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var incomeDatabase: DatabaseReference
    private lateinit var expenseDatabase: DatabaseReference

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

        //Connessione Animation con layout
        fadeOpen = AnimationUtils.loadAnimation(activity,R.anim.fade_open)
        fadeClose = AnimationUtils.loadAnimation(activity,R.anim.fade_close)

        fab_main_btn.setOnClickListener {

            addData()

            floatingButtonAnimation()
        }

        return myView
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