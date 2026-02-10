package com.example.autopark.util

import android.content.Context
import android.net.Uri
import com.example.autopark.data.model.ParkingTransaction
import com.example.autopark.data.model.User
import com.example.autopark.data.model.Vehicle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PDFGenerator {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Generate a driver report PDF with their vehicles, parking history, and charges
     */
    suspend fun generateDriverReport(
        context: Context,
        uri: Uri,
        driver: User,
        vehicles: List<Vehicle>,
        transactions: List<ParkingTransaction>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open output stream"))
            
            generateDriverReportPDF(outputStream, driver, vehicles, transactions)
            Result.success("Driver report generated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate an admin report PDF with system-wide statistics
     */
    suspend fun generateAdminReport(
        context: Context,
        uri: Uri,
        reportData: AdminReportData
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open output stream"))
            
            generateAdminReportPDF(outputStream, reportData)
            Result.success("Admin report generated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateDriverReportPDF(
        outputStream: OutputStream,
        driver: User,
        vehicles: List<Vehicle>,
        transactions: List<ParkingTransaction>
    ) {
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)
        
        // Title
        document.add(
            Paragraph("Driver Parking Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )
        
        // Report Generation Date
        document.add(
            Paragraph("Generated on: ${dateFormat.format(Date())}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
                .setMarginBottom(20f)
        )
        
        // Driver Information Section
        document.add(Paragraph("Driver Information").setBold().setFontSize(14f))
        document.add(createInfoTable(mapOf(
            "Name" to (driver.name.ifEmpty { "N/A" }),
            "Email" to driver.email,
            "Phone" to (driver.phoneNumber.ifEmpty { "N/A" }),
            "License Number" to (driver.licenseNumber.ifEmpty { "N/A" }),
            "VIP Status" to if (driver.isVIP) "Yes" else "No",
            "Total Parkings" to driver.totalParkings.toString(),
            "Total Charges" to CurrencyFormatter.formatCurrency(driver.totalCharges)
        )))
        document.add(Paragraph("").setMarginBottom(10f))
        
        // Vehicles Section
        document.add(Paragraph("Registered Vehicles").setBold().setFontSize(14f))
        if (vehicles.isEmpty()) {
            document.add(Paragraph("No vehicles registered").setItalic())
        } else {
            val vehicleTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1.5f, 1.5f, 1f)))
                .useAllAvailableWidth()
            
            // Header
            vehicleTable.addHeaderCell(createHeaderCell("Vehicle Number"))
            vehicleTable.addHeaderCell(createHeaderCell("Type"))
            vehicleTable.addHeaderCell(createHeaderCell("Brand/Model"))
            vehicleTable.addHeaderCell(createHeaderCell("Status"))
            
            vehicles.forEach { vehicle ->
                vehicleTable.addCell(createCell(vehicle.vehicleNumber))
                vehicleTable.addCell(createCell(vehicle.vehicleType))
                vehicleTable.addCell(createCell("${vehicle.brand} ${vehicle.model}"))
                vehicleTable.addCell(createCell(
                    if (vehicle.parkingLicenseValid) "Valid" else "Invalid"
                ))
            }
            document.add(vehicleTable)
        }
        document.add(Paragraph("").setMarginBottom(10f))
        
        // Parking History Section
        document.add(Paragraph("Parking History").setBold().setFontSize(14f))
        if (transactions.isEmpty()) {
            document.add(Paragraph("No parking transactions found").setItalic())
        } else {
            val transactionTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1.5f, 2f, 1f, 1f)))
                .useAllAvailableWidth()
            
            // Header
            transactionTable.addHeaderCell(createHeaderCell("Vehicle"))
            transactionTable.addHeaderCell(createHeaderCell("Date"))
            transactionTable.addHeaderCell(createHeaderCell("Duration"))
            transactionTable.addHeaderCell(createHeaderCell("Status"))
            transactionTable.addHeaderCell(createHeaderCell("Charge"))
            
            // Calculate totals
            var totalCharges = 0.0
            var totalCompleted = 0
            
            transactions.sortedByDescending { it.entryTime }.forEach { transaction ->
                val duration = if (transaction.exitTime != null) {
                    DateFormatter.getDurationString(transaction.entryTime, transaction.exitTime!!)
                } else {
                    "Active"
                }
                
                transactionTable.addCell(createCell(transaction.vehicleNumber))
                transactionTable.addCell(createCell(dateOnlyFormat.format(Date(transaction.entryTime))))
                transactionTable.addCell(createCell(duration))
                transactionTable.addCell(createCell(transaction.status))
                transactionTable.addCell(createCell(
                    CurrencyFormatter.formatCurrency(transaction.chargeAmount)
                ))
                
                totalCharges += transaction.chargeAmount
                if (transaction.status == "COMPLETED") {
                    totalCompleted++
                }
            }
            document.add(transactionTable)
            
            // Summary
            document.add(Paragraph("").setMarginTop(10f))
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f)))
                .useAllAvailableWidth()
            summaryTable.addCell(createCell("Total Transactions:"))
            summaryTable.addCell(createCell(transactions.size.toString()))
            summaryTable.addCell(createCell("Completed Parkings:"))
            summaryTable.addCell(createCell(totalCompleted.toString()))
            summaryTable.addCell(createCell("Total Charges:"))
            summaryTable.addCell(createCell(CurrencyFormatter.formatCurrency(totalCharges)))
            summaryTable.addCell(createCell("Average Charge:"))
            summaryTable.addCell(createCell(
                CurrencyFormatter.formatCurrency(
                    if (transactions.isNotEmpty()) totalCharges / transactions.size else 0.0
                )
            ))
            document.add(summaryTable)
        }
        
        // Footer
        document.add(Paragraph("").setMarginTop(20f))
        document.add(
            Paragraph("This report is generated by AutoPark System and is confidential.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8f)
                .setItalic()
        )
        
        document.close()
        pdfDocument.close()
    }
    
    private fun generateAdminReportPDF(
        outputStream: OutputStream,
        reportData: AdminReportData
    ) {
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)
        
        // Title
        document.add(
            Paragraph("AutoPark System Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )
        
        // Report Period
        document.add(
            Paragraph("Report Period: ${reportData.period}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12f)
        )
        
        // Report Generation Date
        document.add(
            Paragraph("Generated on: ${dateFormat.format(Date())}")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
                .setMarginBottom(20f)
        )
        
        // System Overview Section
        document.add(Paragraph("System Overview").setBold().setFontSize(14f))
        document.add(createInfoTable(mapOf(
            "Total Users" to reportData.totalUsers.toString(),
            "Total Vehicles" to reportData.totalVehicles.toString(),
            "Total Parking Lots" to reportData.totalParkingLots.toString(),
            "Total Parking Spots" to reportData.totalParkingSpots.toString(),
            "Available Spots" to reportData.availableSpots.toString()
        )))
        document.add(Paragraph("").setMarginBottom(10f))
        
        // Revenue Statistics Section
        document.add(Paragraph("Revenue Statistics").setBold().setFontSize(14f))
        document.add(createInfoTable(mapOf(
            "Total Revenue" to CurrencyFormatter.formatCurrency(reportData.totalRevenue),
            "Normal Rate Revenue" to CurrencyFormatter.formatCurrency(reportData.normalRevenue),
            "VIP Rate Revenue" to CurrencyFormatter.formatCurrency(reportData.vipRevenue),
            "Average Charge per Parking" to CurrencyFormatter.formatCurrency(reportData.averageCharge),
            "Total Transactions" to reportData.totalTransactions.toString(),
            "Completed Transactions" to reportData.completedTransactions.toString()
        )))
        document.add(Paragraph("").setMarginBottom(10f))
        
        // Parking Lot Performance Section
        if (reportData.lotPerformance.isNotEmpty()) {
            document.add(Paragraph("Parking Lot Performance").setBold().setFontSize(14f))
            val lotTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f, 1f, 1f, 1f)))
                .useAllAvailableWidth()
            
            lotTable.addHeaderCell(createHeaderCell("Parking Lot"))
            lotTable.addHeaderCell(createHeaderCell("Total Spots"))
            lotTable.addHeaderCell(createHeaderCell("Available"))
            lotTable.addHeaderCell(createHeaderCell("Occupancy %"))
            lotTable.addHeaderCell(createHeaderCell("Revenue"))
            
            reportData.lotPerformance.forEach { lot ->
                lotTable.addCell(createCell(lot.name))
                lotTable.addCell(createCell(lot.totalSpots.toString()))
                lotTable.addCell(createCell(lot.availableSpots.toString()))
                lotTable.addCell(createCell("${lot.occupancyRate}%"))
                lotTable.addCell(createCell(CurrencyFormatter.formatCurrency(lot.revenue)))
            }
            document.add(lotTable)
            document.add(Paragraph("").setMarginBottom(10f))
        }
        
        // Recent Transactions Section
        if (reportData.recentTransactions.isNotEmpty()) {
            document.add(Paragraph("Recent Transactions").setBold().setFontSize(14f))
            val transTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1.5f, 1.5f, 1f, 1f)))
                .useAllAvailableWidth()
            
            transTable.addHeaderCell(createHeaderCell("Vehicle"))
            transTable.addHeaderCell(createHeaderCell("Parking Lot"))
            transTable.addHeaderCell(createHeaderCell("Entry Time"))
            transTable.addHeaderCell(createHeaderCell("Status"))
            transTable.addHeaderCell(createHeaderCell("Charge"))
            
            reportData.recentTransactions.take(20).forEach { transaction ->
                transTable.addCell(createCell(transaction.vehicleNumber))
                transTable.addCell(createCell(transaction.parkingLotId))
                transTable.addCell(createCell(dateFormat.format(Date(transaction.entryTime))))
                transTable.addCell(createCell(transaction.status))
                transTable.addCell(createCell(
                    CurrencyFormatter.formatCurrency(transaction.chargeAmount)
                ))
            }
            document.add(transTable)
        }
        
        // Footer
        document.add(Paragraph("").setMarginTop(20f))
        document.add(
            Paragraph("This is an official AutoPark System administrative report.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8f)
                .setItalic()
        )
        
        document.close()
        pdfDocument.close()
    }
    
    private fun createInfoTable(data: Map<String, String>): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1.5f, 2f)))
            .useAllAvailableWidth()
        
        data.forEach { (label, value) ->
            table.addCell(createCell(label).setBold())
            table.addCell(createCell(value))
        }
        
        return table
    }
    
    private fun createHeaderCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text).setBold())
            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
            .setPadding(5f)
    }
    
    private fun createCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text))
            .setPadding(5f)
    }
}

/**
 * Data class for admin report
 */
data class AdminReportData(
    val period: String,
    val totalUsers: Int,
    val totalVehicles: Int,
    val totalParkingLots: Int,
    val totalParkingSpots: Int,
    val availableSpots: Int,
    val totalRevenue: Double,
    val normalRevenue: Double,
    val vipRevenue: Double,
    val averageCharge: Double,
    val totalTransactions: Int,
    val completedTransactions: Int,
    val lotPerformance: List<LotPerformance>,
    val recentTransactions: List<ParkingTransaction>
)

data class LotPerformance(
    val name: String,
    val totalSpots: Int,
    val availableSpots: Int,
    val occupancyRate: Int,
    val revenue: Double
)
