package com.example.moneybook

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticFragment : Fragment() {

    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var incomeDatabase: DatabaseReference
    private lateinit var expenseDatabase: DatabaseReference

    //Button mese,anno, tipologia
    private lateinit var btn_mese: Button
    private lateinit var btn_anno: Button
    private lateinit var btn_tipologia: Button
    private lateinit var removeFilter: TextView

    //Risultato totale Entrate/Uscite
    private lateinit var totalIncomeResult: TextView
    private lateinit var totalExpenseResult: TextView

    //RecyclerView Entrate/Uscite
    private lateinit var recyclerIncome: RecyclerView
    private lateinit var recyclerExpense: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_statistic, container, false)

        //Inizializzazione Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        val user: FirebaseUser = firebaseAuth.currentUser!!
        val uid: String = user.uid
        incomeDatabase = FirebaseDatabase.getInstance("https://moneybook-f9f3a-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("IncomeData").child(uid)
        expenseDatabase = FirebaseDatabase.getInstance("https://moneybook-f9f3a-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("ExpenseDatabase").child(uid)

        //Inizializzazione button, recycler , textView
        btn_mese = myView.findViewById(R.id.btnFilter_mese)
        btn_anno = myView.findViewById(R.id.btnFilter_anno)
        btn_tipologia = myView.findViewById(R.id.btnFilter_tipologia)
        removeFilter = myView.findViewById(R.id.filterRemove_string)
        totalIncomeResult = myView.findViewById(R.id.income_set_result)
        totalExpenseResult = myView.findViewById(R.id.expense_set_result)
        recyclerIncome = myView.findViewById(R.id.recycler_income)
        recyclerExpense = myView.findViewById(R.id.recycler_expense)

        //Inizializzazione delle dimensioni del RecyclerView
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

        calcolaEntrate()
        calcolaUscite()

        btn_mese.setOnClickListener {
            // Mostra un dialogo di input per ottenere il mese dall'utente
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Inserisci il mese")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, which ->
                val mese = input.text.toString().trim()
                filtraPerMese(mese)
            }
            builder.setNegativeButton("Annulla") { dialog, which ->
                dialog.cancel()
            }
            builder.show()
        }
        btn_anno.setOnClickListener {
            // Mostra un dialogo di input per ottenere l'anno dall'utente
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Inserisci l'anno")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, which ->
                val anno = input.text.toString().trim()
                filtraPerAnno(anno)
            }
            builder.setNegativeButton("Annulla") { dialog, which ->
                dialog.cancel()
            }
            builder.show()
        }
        btn_tipologia.setOnClickListener {
            // Mostra un dialogo di input per ottenere la tipologia dall'utente
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Inserisci la tipologia")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, which ->
                val tipologia = input.text.toString().trim()
                filtraPerTipologia(tipologia)
            }
            builder.setNegativeButton("Annulla") { dialog, which ->
                dialog.cancel()
            }
            builder.show()
        }

        removeFilter.setOnClickListener {
            onStart()
            calcolaEntrate()
            calcolaUscite()
        }

        return myView
    }
    private fun filtraPerMese(mese: String) {
        val filterMeseIncomeQuery = incomeDatabase.orderByChild("mese").equalTo(mese)
        val filterMeseExpenseQuery = expenseDatabase.orderByChild("mese").equalTo(mese)
        //codice per filter su entrate
        val optionsIn: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(filterMeseIncomeQuery, Data::class.java)
            .build()
        val incomeAdapter = object : FirebaseRecyclerAdapter<Data, IncomeViewHolder>(optionsIn) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboarad_income, parent, false)
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
        //codice per filter su uscite
        val optionsOut: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(filterMeseExpenseQuery, Data::class.java)
            .build()
        val expenseAdapter = object : FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(optionsOut){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_expense,parent, false)
                return  ExpenseViewHolder(view)
            }

            override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int, model: Data) {
                holder.setExpenseType(model.type)
                holder.setExpenseAmount(model.amount)
                holder.setExpenseDate(model.date)
            }
        }
        recyclerExpense.adapter = expenseAdapter
        expenseAdapter.startListening()
        calcolaEntrateQueryConFiltro(filterMeseIncomeQuery)
        calcolaUsciteQueryConFiltro(filterMeseExpenseQuery)

    }
    private fun filtraPerAnno(anno: String) {
        val filterAnnoIncomeQuery = incomeDatabase.orderByChild("anno").equalTo(anno)
        val filterAnnoExpenseQuery = expenseDatabase.orderByChild("anno").equalTo(anno)
        //codice per filter su entrate
        val optionsIn: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(filterAnnoIncomeQuery, Data::class.java)
            .build()
        val incomeAdapter = object : FirebaseRecyclerAdapter<Data, IncomeViewHolder>(optionsIn) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboarad_income, parent, false)
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
        //codice per filter su uscite
        val optionsOut: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(filterAnnoExpenseQuery, Data::class.java)
            .build()
        val expenseAdapter = object : FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(optionsOut){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_expense,parent, false)
                return  ExpenseViewHolder(view)
            }

            override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int, model: Data) {
                holder.setExpenseType(model.type)
                holder.setExpenseAmount(model.amount)
                holder.setExpenseDate(model.date)
            }
        }
        recyclerExpense.adapter = expenseAdapter
        expenseAdapter.startListening()
        calcolaEntrateQueryConFiltro(filterAnnoIncomeQuery)
        calcolaUsciteQueryConFiltro(filterAnnoExpenseQuery)
    }

    private fun filtraPerTipologia(tipologia: String) {
        val filteredIncomeQuery = incomeDatabase.orderByChild("type").equalTo(tipologia)
        val filteredExpenseQuery = expenseDatabase.orderByChild("type").equalTo(tipologia)

        //codice per filter su entrate
        val optionsIn: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(filteredIncomeQuery, Data::class.java)
            .build()
        val incomeAdapter = object : FirebaseRecyclerAdapter<Data, IncomeViewHolder>(optionsIn) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboarad_income, parent, false)
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

        //codice per filter su uscite
        val optionsOut: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(filteredExpenseQuery, Data::class.java)
            .build()
        val expenseAdapter = object : FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(optionsOut){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_expense,parent, false)
                return  ExpenseViewHolder(view)
            }

            override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int, model: Data) {
                holder.setExpenseType(model.type)
                holder.setExpenseAmount(model.amount)
                holder.setExpenseDate(model.date)
            }
        }
        recyclerExpense.adapter = expenseAdapter
        expenseAdapter.startListening()

        calcolaEntrateQueryConFiltro(filteredIncomeQuery)
        calcolaUsciteQueryConFiltro(filteredExpenseQuery)
    }


    //Caricamento recyclerView al load del StatisticFragment
    override fun onStart() {
        super.onStart()

        val optionsIn: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(incomeDatabase, Data::class.java)
            .build()
        val incomeAdapter = object : FirebaseRecyclerAdapter<Data, IncomeViewHolder>(optionsIn){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboarad_income,parent,false)
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

        val optionsOut: FirebaseRecyclerOptions<Data> = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(expenseDatabase, Data::class.java)
            .build()

        val expenseAdapter = object: FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(optionsOut){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_expense,parent,false)
                return ExpenseViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ExpenseViewHolder, position: Int, model: Data) {
                viewHolder.setExpenseType(model.type)
                viewHolder.setExpenseAmount(model.amount)
                viewHolder.setExpenseDate(model.date)
            }
        }
        recyclerExpense.adapter = expenseAdapter
        expenseAdapter.startListening()
    }

    fun calcolaEntrate(){
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
    }

    fun calcolaUscite(){
        expenseDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sommaTotaleUScite = 0
                for (mySnapshot in dataSnapshot.children){
                    val data = mySnapshot.getValue(Data::class.java)
                    sommaTotaleUScite+=data?.amount?:0
                }
                val strSommaTot = sommaTotaleUScite.toString()
                totalExpenseResult.text="$strSommaTot.00"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun calcolaEntrateQueryConFiltro(query: Query){
        query.addValueEventListener(object : ValueEventListener{
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
    }

    fun calcolaUsciteQueryConFiltro(query: Query){
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var sommaTotaleUScite = 0
                for (mySnapshot in dataSnapshot.children){
                    val data = mySnapshot.getValue(Data::class.java)
                    sommaTotaleUScite+=data?.amount?:0
                }
                val strSommaTot = sommaTotaleUScite.toString()
                totalExpenseResult.text="$strSommaTot.00"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
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

    inner class ExpenseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var myExpenseView: View = itemView

        fun setExpenseType(type: String){
            var mType: TextView = myExpenseView.findViewById(R.id.type_expense_ds)
            mType.text = type
        }

        fun setExpenseAmount(amount: Int){
            var mAmount: TextView = myExpenseView.findViewById(R.id.amount_expense_ds)
            var strAmount = amount.toString()
            mAmount.text = strAmount
        }

        fun setExpenseDate(date: String){
            var mDate: TextView = myExpenseView.findViewById(R.id.date_expense_ds)
            mDate.text = date
        }
    }
}