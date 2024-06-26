package my.edu.tarc.debtdecoderApp.repaymentTab.strategiesTab

import my.edu.tarc.debtdecoderApp.repaymentTab.LoanDatabaseHelper
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import my.edu.tarc.debtdecoder.databinding.FragmentStrategyDetailsBinding
import my.edu.tarc.debtdecoderApp.income.IncomeViewModel
import my.edu.yyass.Loan
import my.edu.tarc.debtdecoderApp.repaymentTab.LoanAdapter

class StrategyDetailsDialogFragment : DialogFragment() {

    private var _binding: FragmentStrategyDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var loanAdapter: LoanAdapter
    private lateinit var  incomeViewModel: IncomeViewModel

    companion object {
        fun newInstance(strategy: String): StrategyDetailsDialogFragment {
            val fragment = StrategyDetailsDialogFragment()
            val args = Bundle()
            args.putString("strategyType", strategy)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // Make the dialog background transparent
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStrategyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        // Retrieve the strategy from the arguments
        val strategy = arguments?.getString("strategyType") ?: return
        loanAdapter.strategyType = strategy
        loadLoans(strategy)

        incomeViewModel = ViewModelProvider(requireActivity()).get(IncomeViewModel::class.java)
        incomeViewModel.currentMonthIncome.observe(viewLifecycleOwner, Observer { income ->
            Log.e("Check Income Value", "onViewCreated: $income")
            loadLoans(strategy)
            updateIncomeRecalculate(income.toDouble())
        })
    }

    private fun setupRecyclerView() {
        loanAdapter = LoanAdapter()
        binding.loansRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = loanAdapter
        }
    }

    private fun loadLoans(strategy: String) {
        val loans = getLoansBasedOnStrategy(strategy)
        loanAdapter.strategyType = strategy
        loanAdapter.submitList(loans)
        loanAdapter.notifyDataSetChanged()
    }

    private fun updateIncomeRecalculate(income: Double){
        loanAdapter.updateIncome(income)
    }

    private fun getLoansBasedOnStrategy(strategy: String): List<Loan> {
        val dbHelper = LoanDatabaseHelper(requireContext())
        val borrowLoans = dbHelper.getAllLoans().filter { it.type == "borrow" }
        val lendLoans = dbHelper.getAllLoans().filter { it.type == "lend" }

        return when(strategy) {
            "Borrow" -> borrowLoans
            "Avalanche" -> borrowLoans.sortedByDescending { it.interest }
            "Snowball" -> borrowLoans.sortedBy { it.amount }
            "OwedToYou" -> lendLoans
            else -> dbHelper.getAllLoans()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
