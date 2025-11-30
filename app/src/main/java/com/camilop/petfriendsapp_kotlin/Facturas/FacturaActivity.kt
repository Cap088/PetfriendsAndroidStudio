package com.camilop.petfriendsapp_kotlin.Facturas

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.camilop.petfriendsapp_kotlin.databinding.ActivityFacturaBinding
import com.camilop.petfriendsapp_kotlin.models.Cliente
import com.camilop.petfriendsapp_kotlin.models.VentaCompleta
import com.camilop.petfriendsapp_kotlin.models.DetalleVenta
import com.camilop.petfriendsapp_kotlin.Facturas.adapters.ProductosFacturaAdapter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.camilop.petfriendsapp_kotlin.R


class FacturaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacturaBinding
    private val viewModel: FacturaViewModel by viewModels()
    private var ventaCompleta: VentaCompleta? = null
    private var cliente: Cliente? = null
    private val IVA_PERCENTAGE = 16

    // Modelo de datos para los totales
    data class TotalesFactura(
        val subtotal: Double,
        val descuento: Double,
        val ivaCalculado: Double,
        val totalFinal: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idVenta = intent.getIntExtra("ID_VENTA", 0)

        if (idVenta == 0) {
            Toast.makeText(this, "Error: No se recibió ID de venta", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        viewModel.loadFacturaData(idVenta)
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnVolver.setOnClickListener { finish() }
        binding.btnCompartir.setOnClickListener {
            ventaCompleta?.let {
                generarYCompartirPDF(it)
            } ?: run {
                Toast.makeText(this, "Espere a que carguen los datos", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar RecyclerView
        binding.rvProductosFactura.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.facturaData.observe(this) { ventaCompleta ->
            if (ventaCompleta != null) {
                println("FACTURA CARGADA: ${ventaCompleta.cabecera.idCabeceraVenta}")
                println("PRODUCTOS: ${ventaCompleta.detalle.size}")
                this.ventaCompleta = ventaCompleta
                mostrarDatosFactura(ventaCompleta)
                binding.progressBar.visibility = View.GONE
            } else {
                println(" FACTURA NULL")
            }
        }

        viewModel.clienteData.observe(this) { cliente ->
            if (cliente != null) {
                println("CLIENTE CARGADO: ${cliente.nombre}")
                this.cliente = cliente
                mostrarDatosCliente(cliente)
            } else {
                println(" CLIENTE NULL")
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            println("LOADING: $isLoading")
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                println(" ERROR: $it")
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun mostrarDatosCliente(cliente: Cliente) {
        binding.tvClienteNombre.text = "${cliente.nombre} ${cliente.apellido}"
        binding.tvClienteCedula.text = "Cédula: ${cliente.cedula}"
        binding.tvClienteDireccion.text = "Dirección: ${cliente.direccion}"
        binding.tvClienteTelefono.text = "Teléfono: ${cliente.telefono}"
    }

    private fun mostrarDatosFactura(ventaCompleta: VentaCompleta) {
        with(ventaCompleta.cabecera) {
            binding.tvNumeroFactura.text = "Factura #: $idCabeceraVenta"
            binding.tvFecha.text = "Fecha: $fechaVenta"
            val metodoPago = if (!tarjeta.isNullOrEmpty()) {
                "$tarjeta ${numeroTarjeta ?: ""}"
            } else {
                "Efectivo"
            }
            binding.tvMetodoPago.text = "Forma de pago: $metodoPago"
        }

        // Configurar RecyclerView para productos
        if (ventaCompleta.detalle.isNotEmpty()) {
            val productosAdapter = ProductosFacturaAdapter(ventaCompleta.detalle)
            binding.rvProductosFactura.adapter = productosAdapter
            binding.rvProductosFactura.visibility = View.VISIBLE
        } else {
            binding.rvProductosFactura.visibility = View.GONE
            Toast.makeText(this, "No hay productos en esta venta", Toast.LENGTH_SHORT).show()
        }

        // USAR LA FUNCIÓN DE SUMATORIA SIMPLE
        val totales = calcularTotales(ventaCompleta.detalle)
        mostrarTotalesEnVista(totales)
    }

    //FUNCIÓN CORREGIDA: Simplemente suma los valores de totales que ya vienen de la API.
    //Esto evita recalcular el 16% que ya se calculó en el pago.

    private fun calcularTotales(detalles: List<DetalleVenta>): TotalesFactura {
        val subtotal = detalles.sumOf { it.subtotal }
        val descuento = detalles.sumOf { it.descuento }
        val iva = detalles.sumOf { it.iva }
        val total = detalles.sumOf { it.totalPagar }

        return TotalesFactura(
            subtotal = subtotal,
            descuento = descuento,
            ivaCalculado = iva,      // Usamos el valor 'iva' que viene de la API
            totalFinal = total
        )
    }

    /**
     * Muestra los totales calculados en los TextViews
     */
    private fun mostrarTotalesEnVista(totales: TotalesFactura) {
        // La operación de String.format ya muestra el valor en pantalla
        binding.tvSubtotal.text = "$${String.format("%.2f", totales.subtotal)}"
        binding.tvDescuento.text = "$${String.format("%.2f", totales.descuento)}"
        binding.tvIva.text =
            "$${String.format("%.2f", totales.ivaCalculado)}" // Muestra el valor de IVA sumado de la API
        binding.tvTotal.text =
            "$${String.format("%.2f", totales.totalFinal)}"   // Muestra el total sumado de la API
    }

    private fun generarYCompartirPDF(ventaCompleta: VentaCompleta) {
        try {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnCompartir.isEnabled = false

            println("Iniciando generación de PDF...")

            // Crear directorio
            val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "facturas")
            println("Directorio: $storageDir")
            println("Existe: ${storageDir.exists()}")

            if (!storageDir.exists()) {
                val created = storageDir.mkdirs()
                println("Directorio creado: $created")
            }

            println("Puede escribir: ${storageDir.canWrite()}")

            // Generar PDF
            val pdfFile = crearPDF(ventaCompleta)

            // Verificar resultado
            println("PDF generado en: ${pdfFile.absolutePath}")
            println("Existe: ${pdfFile.exists()}")
            println("Tamaño: ${pdfFile.length()} bytes")

            if (pdfFile.exists() && pdfFile.length() > 0) {
                println("PDF generado exitosamente")

                // Compartir PDF
                compartirPDF(pdfFile)

                Toast.makeText(this, "PDF generado y listo para compartir", Toast.LENGTH_SHORT).show()
            } else {
                throw Exception("El archivo PDF no se creó correctamente. Existe: ${pdfFile.exists()}, Tamaño: ${pdfFile.length()}")
            }

        } catch (e: Exception) {
            println(" ERROR CAPTURADO en generarYCompartirPDF:")
            println(" Mensaje: ${e.message}")
            println(" Causa: ${e.cause}")
            println(" StackTrace:")
            e.printStackTrace()

            Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            binding.progressBar.visibility = View.GONE
            binding.btnCompartir.isEnabled = true
        }
    }

    private fun crearPDF(ventaCompleta: VentaCompleta): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Factura_PetFriends_${ventaCompleta.cabecera.idCabeceraVenta}_$timeStamp.pdf"

        // Usar el directorio de archivos externos privados de la app
        val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "facturas")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val pdfFile = File(storageDir, fileName)

        // Crear PDF
        val outputStream = FileOutputStream(pdfFile)
        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            document.setMargins(20f, 20f, 20f, 20f)

            // Título de texto
            val titulo = Paragraph("FACTURA")
                .setFontSize(20f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE)
            document.add(titulo)

            document.add(Paragraph(" "))

            //CÓDIGO CORREGIDO: CARGAR IMAGEN DESDE DRAWABLE

            try {
                println("Agregando Logo desde drawable...")

                // 1. Obtener el recurso Drawable
                val drawable = resources.getDrawable(R.drawable.logo, null)

                // 2. Convertir Drawable a Bitmap
                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    ?: throw IllegalStateException("Drawable no se pudo convertir a Bitmap")

                // 3. Comprimir el Bitmap a un ByteArray (usando PNG para mejor calidad sin pérdida)
                val stream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                val imageDataBytes = stream.toByteArray()

                // 4. Crear el objeto ImageData e Image de iText
                val imageData = ImageDataFactory.create(imageDataBytes)
                val logo = Image(imageData)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setWidth(UnitValue.createPercentValue(20f)) // Usar porcentaje para ser responsivo
                    .setMarginBottom(10f)

                document.add(logo)
                println("Logo agregado correctamente.")

            } catch (e: Exception) {
                // Si la carga del logo falla, imprimimos el error y continuamos sin él
                println(" ERROR CRÍTICO al generar el logo desde drawable: ${e.message}")
                e.printStackTrace()
                // Agrega una nota de texto para indicar que el logo falló
                document.add(Paragraph("[Error al cargar Logo: ${e.message}]").setTextAlignment(TextAlignment.CENTER))
            }
            // =========================================================

            // Informacion de la factura
            println("Agregando información de factura...")
            val infoTable = Table(2)
            infoTable.setWidth(UnitValue.createPercentValue(100f))

            // la tabla de información y el documento

            infoTable.addCell(crearCelda("Número de Factura:", true))
            infoTable.addCell(crearCelda(ventaCompleta.cabecera.idCabeceraVenta.toString()))
            infoTable.addCell(crearCelda("Fecha:", true))
            infoTable.addCell(crearCelda(ventaCompleta.cabecera.fechaVenta))
            infoTable.addCell(crearCelda("Método de Pago:", true))
            infoTable.addCell(crearCelda(
                if (!ventaCompleta.cabecera.tarjeta.isNullOrEmpty())
                    "${ventaCompleta.cabecera.tarjeta} ${ventaCompleta.cabecera.numeroTarjeta ?: ""}"
                else "Efectivo"
            ))

            document.add(infoTable)
            document.add(Paragraph(" "))

            // Información del cliente
            println("Agregando información del cliente...")
            cliente?.let {
                val clienteHeader = Paragraph("INFORMACIÓN DEL CLIENTE")
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                document.add(clienteHeader)

                val clienteTable = Table(2)
                clienteTable.setWidth(UnitValue.createPercentValue(100f))

                clienteTable.addCell(crearCelda("Nombre:", true))
                clienteTable.addCell(crearCelda("${it.nombre} ${it.apellido}"))
                clienteTable.addCell(crearCelda("Cédula:", true))
                clienteTable.addCell(crearCelda(it.cedula))
                clienteTable.addCell(crearCelda("Dirección:", true))
                clienteTable.addCell(crearCelda(it.direccion))
                clienteTable.addCell(crearCelda("Teléfono:", true))
                clienteTable.addCell(crearCelda(it.telefono))

                document.add(clienteTable)
                document.add(Paragraph(" "))
            }

            // Productos
            println("Agregando productos...")
            val productosHeader = Paragraph("PRODUCTOS")
                .setBold()
                .setFontColor(ColorConstants.BLUE)
            document.add(productosHeader)

            val productosTable = Table(4)
            productosTable.setWidth(UnitValue.createPercentValue(100f))

            // Encabezados de la tabla
            productosTable.addHeaderCell(crearCelda("Producto", true))
            productosTable.addHeaderCell(crearCelda("Cantidad", true))
            productosTable.addHeaderCell(crearCelda("Precio Unit.", true))
            productosTable.addHeaderCell(crearCelda("Subtotal", true))

            // Datos de productos
            ventaCompleta.detalle.forEach { detalle ->
                productosTable.addCell(crearCelda(detalle.nombreProducto))
                productosTable.addCell(crearCelda(detalle.cantidad.toString()))
                productosTable.addCell(crearCelda("$${String.format("%.2f", detalle.precioUnitario)}"))
                productosTable.addCell(crearCelda("$${String.format("%.2f", detalle.subtotal)}"))
            }

            document.add(productosTable)
            document.add(Paragraph(" "))

            // Totales
            println("Agregando totales...")

            // LLAMADA A LA FUNCIÓN DE SUMATORIA SIMPLE
            val totales = calcularTotales(ventaCompleta.detalle)

            val totalesTable = Table(2)
            totalesTable.setWidth(UnitValue.createPercentValue(60f))
            totalesTable.setHorizontalAlignment(HorizontalAlignment.RIGHT)

            totalesTable.addCell(crearCelda("Subtotal:", true))
            totalesTable.addCell(crearCelda("$${String.format("%.2f", totales.subtotal)}"))
            totalesTable.addCell(crearCelda("Descuento:", true))
            totalesTable.addCell(crearCelda("$${String.format("%.2f", totales.descuento)}"))

            // USAMOS EL VALOR DE IVA CALCULADO Y SUMADO DE LA API
            totalesTable.addCell(crearCelda("IVA (${IVA_PERCENTAGE}%)", true))
            totalesTable.addCell(crearCelda("$${String.format("%.2f", totales.ivaCalculado)}", false))

            totalesTable.addCell(crearCelda("TOTAL:", true))
            totalesTable.addCell(crearCelda("$${String.format("%.2f", totales.totalFinal)}", true))

            document.add(totalesTable)

            // Pie de página
            println("Agregando pie de página...")
            document.add(Paragraph(" ").setMarginTop(20f))
            val pie = Paragraph("Gracias por su compra - PetFriends App")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontColor(ColorConstants.GRAY)
            document.add(pie)

            println(" Documento PDF creado exitosamente")

        } finally {
            println("Cerrando documento...")
            document.close()
            pdfDocument.close()
            writer.close()
            outputStream.close()
            println("Recursos liberados")
        }

        return pdfFile
    }

    private fun crearCelda(texto: String?, esEncabezado: Boolean = false): Cell {
        // Manejar valores nulos o vacíos
        val textoSeguro = texto ?: "N/A"
        val textoFinal = if (textoSeguro.isEmpty()) "N/A" else textoSeguro

        return Cell().add(Paragraph(textoFinal).apply {
            if (esEncabezado) {
                setBold()
            }
        }).setPadding(5f)
    }

    private fun compartirPDF(pdfFile: File) {
        try {
            // Verificar que el archivo existe antes de compartir
            if (!pdfFile.exists()) {
                Toast.makeText(this, "El archivo PDF no existe", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                pdfFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Factura #${ventaCompleta?.cabecera?.idCabeceraVenta}")
                putExtra(Intent.EXTRA_TEXT, "Adjunto encontrará la factura #${ventaCompleta?.cabecera?.idCabeceraVenta} de PetFriends")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Compartir factura PDF"))

        } catch (e: Exception) {
            println("Error al compartir: ${e.message}")
            Toast.makeText(this, "Error al compartir: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }



}