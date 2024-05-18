package com.example.moneybook

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneybook.Model.Data
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncomeFragment : Fragment() {

    //Firebase database
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var incomeDatabase: DatabaseReference

    //RecyclerView
    private lateinit var recyclerView: RecyclerView

    //TextView
    private lateinit var incomeTotalAmount: TextView

    //Update editText e Button
    private lateinit var edtAmount: EditText
    private lateinit var edtType: EditText
    private lateinit var edtNote: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    //Data per la modifica dei valori
    private lateinit var type: String
    private lateinit var note: String
    private var amount: Int = 0
    private lateinit var postKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_income, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val user: FirebaseUser = firebaseAuth.currentUser!!
        val uid: String = user.uid

        //Firebase di default usa getInstance posizionato in us-central ,se db è posizionato in europa va aggiunto il link a mano per dare la posizione di ricerca corretta
        incomeDatabase = FirebaseDatabase.getInstance("https://moneybook-f9f3a-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("IncomeData").child(uid)
        incomeTotalAmount = myView.findViewById(R.id.income_text_result)

        recyclerView = myView.findViewById(R.id.recycler_id_income)

        val layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        //prende la somma di tutte le entrate, anche in caso di aggiunta nuova Entrata
        incomeDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalValue = 0
                for(mySnapshot in dataSnapshot.children){
                    val data = mySnapshot.getValue(Data::class.java)
                    totalValue+=data?.amount?: 0 //se c'è un valore lo aggiunge, altrimenti 0
                }

                val stringTotalValue = totalValue.toString()
                incomeTotalAmount.text = "$stringTotalValue.00"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        return myView
    }

    override fun onStart(){
        super.onStart()

            val options: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(incomeDatabase, Data::class.java)
                .build()

            val adapter = object : FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.income_recycler_data, parent, false)
                    return MyViewHolder(view)
                }

                override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int, model: Data) {
                    viewHolder.setType(model.type)
                    viewHolder.setNote(model.note)
                    viewHolder.setDate(model.date)
                    viewHolder.setAmount(model.amount)

                    viewHolder.mView.setOnClickListener{
                        postKey = getRef(position).key!!
                        type = model.type
                        note = model.note
                        amount = model.amount
                        updateItem()
                    }
                }
            }

        recyclerView.adapter = adapter
        adapter.startListening()

    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mView: View = itemView

        fun setType(type: String){
            val mType: TextView = mView.findViewById(R.id.type_text_income)
            mType.text = type
        }

        fun setNote(note: String){
            val mNote: TextView = mView.findViewById(R.id.note_text_income)
            mNote.text = note
        }

        fun setDate(date: String){
            val mDate: TextView = mView.findViewById(R.id.date_text_income)
            mDate.text = date
        }

        fun setAmount(amount: Int){
            val mAmount: TextView = mView.findViewById(R.id.amount_text_income)
            val stringAmount = amount.toString()
            mAmount.text = stringAmount
        }
    }

    fun updateItem(){
        val myDialog = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from((activity))
        val myView = inflater.inflate(R.layout.update_data_item,null)
        myDialog.setView(myView)

        edtAmount = myView.findViewById(R.id.amount_edt)
        edtType = myView.findViewById(R.id.type_edt)
        edtNote = myView.findViewById(R.id.note_edt)

        //Set data alle editText
        edtType.setText(type)
        edtType.setSelection(type.length)

        edtNote.setText(note)
        edtNote.setSelection(note.length)

        edtAmount.setText(amount.toString())
        edtAmount.setSelection(amount.toString().length)


        btnUpdate = myView.findViewById(R.id.btnUpdUpdate)
        btnDelete = myView.findViewById(R.id.btnUpdDelete)
        val dialog: AlertDialog = myDialog.create()

        //al click, salvataggio dati modificati su firebase
        btnUpdate.setOnClickListener {
            type = edtType.text.toString().trim()
            note = edtNote.text.toString().trim()
            var strAmountModified = amount.toString()
            strAmountModified = edtAmount.text.toString().trim()
            var myAmount = strAmountModified.toInt()
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val mDate: String = dateFormat.format(Date())
            val data =  Data(myAmount,type,note,postKey,mDate)

            //modifica sul database
            incomeDatabase.child(postKey).setValue(data)

            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            incomeDatabase.child(postKey).removeValue()
            dialog.dismiss()
        }

        dialog.show()
    }

}