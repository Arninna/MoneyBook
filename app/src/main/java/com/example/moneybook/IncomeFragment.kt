package com.example.moneybook

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class IncomeFragment : Fragment() {

    //Firebase database
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var incomeDatabase: DatabaseReference

    //RecyclerView
    private lateinit var recyclerView: RecyclerView

    //TextView
    private lateinit var incomeTotalAmount: TextView

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
                incomeTotalAmount.text = stringTotalValue
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

}