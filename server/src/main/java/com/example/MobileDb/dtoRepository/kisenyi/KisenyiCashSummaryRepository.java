package com.example.MobileDb.dtoRepository.kisenyi;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.Expense;
@Repository
public interface KisenyiCashSummaryRepository extends CrudRepository<Expense, Integer> {

	@Query(value = "SELECT COALESCE(SUM(Selling_amount),0) FROM selling WHERE DATE(Selling_date) = DATE(:date)", nativeQuery = true)
	Integer getSellingTotal(@Param("date") String date);

	@Query(value = "SELECT COALESCE((uganda_opening + uganda_cash_in),0) FROM open_and_close WHERE DATE(date) = DATE(:date)", nativeQuery = true)
	Integer getUgandaOpeningCash(@Param("date") String date);

	@Query(value = "SELECT COALESCE(SUM(Buying_amount * Buying_rate),0) FROM buying WHERE DATE(Buying_date) = DATE(:date)", nativeQuery = true)
	Integer getBuyingTotal(@Param("date") String date);

	@Query(value = "SELECT COALESCE(SUM(expense_amount),0) FROM expenses WHERE DATE(expences_date) = DATE(:date)", nativeQuery = true)
	Integer getExpensesTotal(@Param("date") String date);

	@Query(value = "SELECT COALESCE(uganda_closing,0) FROM open_and_close WHERE DATE(date) = DATE(:date)", nativeQuery = true)
	Integer getFinalClosingCash(@Param("date") String date);

}
