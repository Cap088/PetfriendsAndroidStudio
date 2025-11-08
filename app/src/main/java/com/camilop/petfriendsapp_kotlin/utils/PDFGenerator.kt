package com.camilop.petfriendsapp_kotlin.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDFGenerator(private val context: Context) {

    fun generateInvoicePDF(
        numeroFactura: String,
        fecha: String,
        clienteNombre: String,
        clienteApellido: String,
        clienteCedula: String,
        clienteDireccion: String,
        items: List<InvoiceItem>,
        subtotal: Double,
        iva: Double,
        total: Double,
        onSuccess: (file: File) -> Unit,
        onError: (error: String) -> Unit
    ) {
        try {
            // Crear directorio si no existe
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = File(downloadsDir, "SIVAPP_Facturas")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            // Crear nombre del archivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Factura_${numeroFactura}_$timestamp.pdf"
            val file = File(appDir, fileName)

            // Crear PDF
            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Encabezado de la factura
            addHeader(document, numeroFactura, fecha)

            // Información del cliente
            addClientInfo(document, clienteNombre, clienteApellido, clienteCedula, clienteDireccion)

            // Tabla de productos
            addItemsTable(document, items)

            // Totales
            addTotals(document, subtotal, iva, total)

            // Pie de página
            addFooter(document)

            document.close()

            onSuccess(file)
        } catch (e: Exception) {
            onError("Error al generar PDF: ${e.message}")
        }
    }

    private fun addHeader(document: Document, numeroFactura: String, fecha: String) {
        // Título
        val title = Paragraph("FACTURA")
            .setBold()
            .setFontSize(18f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10f)

        document.add(title)

        // Información de la factura
        val infoTable = Table(2)
        infoTable.setWidth(UnitValue.createPercentValue(100f))

        infoTable.addCell(createCell("N° Factura:", true))
        infoTable.addCell(createCell(numeroFactura, false))
        infoTable.addCell(createCell("Fecha:", true))
        infoTable.addCell(createCell(fecha, false))

        document.add(infoTable)
        document.add(Paragraph(" ")) // Espacio
    }

    private fun addClientInfo(
        document: Document,
        nombre: String,
        apellido: String,
        cedula: String,
        direccion: String
    ) {
        val clientHeader = Paragraph("INFORMACIÓN DEL CLIENTE")
            .setBold()
            .setFontSize(12f)
            .setMarginBottom(5f)

        document.add(clientHeader)

        val clientTable = Table(2)
        clientTable.setWidth(UnitValue.createPercentValue(100f))

        clientTable.addCell(createCell("Nombre:", true))
        clientTable.addCell(createCell("$nombre $apellido", false))
        clientTable.addCell(createCell("Cédula:", true))
        clientTable.addCell(createCell(cedula, false))
        clientTable.addCell(createCell("Dirección:", true))
        clientTable.addCell(createCell(direccion, false))

        document.add(clientTable)
        document.add(Paragraph(" ")) // Espacio
    }

    private fun addItemsTable(document: Document, items: List<InvoiceItem>) {
        val itemsHeader = Paragraph("DETALLE DE PRODUCTOS")
            .setBold()
            .setFontSize(12f)
            .setMarginBottom(5f)

        document.add(itemsHeader)

        val table = Table(5)
        table.setWidth(UnitValue.createPercentValue(100f))

        // Encabezados de la tabla
        table.addHeaderCell(createHeaderCell("Producto"))
        table.addHeaderCell(createHeaderCell("Cantidad"))
        table.addHeaderCell(createHeaderCell("P. Unitario"))
        table.addHeaderCell(createHeaderCell("IVA"))
        table.addHeaderCell(createHeaderCell("Total"))

        // Items
        items.forEach { item ->
            table.addCell(createCell(item.producto, false))
            table.addCell(createCell(item.cantidad.toString(), false))
            table.addCell(createCell(formatCurrency(item.precioUnitario), false))
            table.addCell(createCell(formatCurrency(item.iva), false))
            table.addCell(createCell(formatCurrency(item.total), false))
        }

        document.add(table)
        document.add(Paragraph(" ")) // Espacio
    }

    private fun addTotals(document: Document, subtotal: Double, iva: Double, total: Double) {
        val totalsTable = Table(2)
        totalsTable.setWidth(UnitValue.createPercentValue(50f))
        totalsTable.setHorizontalAlignment(TextAlignment.RIGHT)

        totalsTable.addCell(createCell("Subtotal:", true))
        totalsTable.addCell(createCell(formatCurrency(subtotal), false))
        totalsTable.addCell(createCell("IVA:", true))
        totalsTable.addCell(createCell(formatCurrency(iva), false))
        totalsTable.addCell(createCell("TOTAL:", true))
        totalsTable.addCell(createCell(formatCurrency(total), false).setBold())

        document.add(totalsTable)
    }

    private fun addFooter(document: Document) {
        document.add(Paragraph(" ")) // Espacio
        val footer = Paragraph("Gracias por su compra\nSIVAPP - Sistema de Ventas")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10f)
            .setItalic()

        document.add(footer)
    }

    private fun createCell(text: String, isBold: Boolean): Cell {
        val cell = Cell().add(Paragraph(text))
        if (isBold) {
            cell.setBold()
        }
        cell.setPadding(5f)
        return cell
    }

    private fun createHeaderCell(text: String): Cell {
        return Cell().add(Paragraph(text))
            .setBold()
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(5f)
    }

    private fun formatCurrency(amount: Double): String {
        return "$${String.format(Locale.getDefault(), "%,.2f", amount)}"
    }
}

private fun Table.setHorizontalAlignment(alignment: com.itextpdf.layout.properties.TextAlignment) {}

data class InvoiceItem(
    val producto: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val iva: Double,
    val total: Double
)