package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = false, dynamicColor = false) {
        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          CreditCalculatorScreen(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          )
        }
      }
    }
  }
}

enum class CreditTenor(val months: Int, val factor: Double, val label: String) {
  TENOR_3(3, 0.368, "3 Bulan"),
  TENOR_6(6, 0.208, "6 Bulan"),
  TENOR_9(9, 0.1475, "9 Bulan"),
  TENOR_12(12, 0.121, "12 Bulan")
}

// Helper formatting utilities
fun formatRupiah(amount: Long): String {
  val formatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))
  return "Rp ${formatter.format(amount)}"
}

fun formatNumericString(input: String): String {
  val numberStr = input.filter { it.isDigit() }
  val value = numberStr.toLongOrNull() ?: return ""
  return NumberFormat.getIntegerInstance(Locale("id", "ID")).format(value)
}

fun String.toRawDigits(): String {
  return this.filter { it.isDigit() }
}

@Composable
fun CreditCalculatorScreen(modifier: Modifier = Modifier) {
  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()

  // State Management
  var hargaBarangInput by remember { mutableStateOf("") }
  var dpInput by remember { mutableStateOf("") }
  var isDpAutoFilled by remember { mutableStateOf(true) }
  var selectedTenor by remember { mutableStateOf(CreditTenor.TENOR_12) }
  var bayarAdmin by remember { mutableStateOf(true) }

  // Parsed Numeric Values
  val rawHargaValue = hargaBarangInput.toRawDigits().toLongOrNull() ?: 0L
  val rawDpAmount = dpInput.toRawDigits().toLongOrNull() ?: 0L

  // Validation
  val isPriceValid = rawHargaValue > 0L
  val minDpRequired = if (isPriceValid) (rawHargaValue * 0.20).toLong() else 0L
  val isDpValid = if (isPriceValid) rawDpAmount >= minDpRequired else true
  
  // Real DP Percentage
  val dpPercentage = if (isPriceValid && rawDpAmount > 0L) {
    ((rawDpAmount.toDouble() / rawHargaValue.toDouble()) * 100).toInt()
  } else if (isPriceValid) {
    20
  } else {
    0
  }

  // Credit Calculations
  val pokokUtang = if (rawHargaValue > rawDpAmount) rawHargaValue - rawDpAmount else 0L
  val cicilanPerBulan = (pokokUtang * selectedTenor.factor).toLong()
  val totalKewajiban = cicilanPerBulan * selectedTenor.months
  val biayaAdmin = if (bayarAdmin) 200000L else 0L
  val grandTotal = if (isPriceValid) (totalKewajiban + rawDpAmount + biayaAdmin) else 0L

  Box(
    modifier = modifier
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      
      // HEADER SECTION
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(4.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
      ) {
        Row(
          modifier = Modifier.padding(20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.primary)
              .padding(12.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ShoppingCart,
              contentDescription = "Calculator Icon",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
              text = "Kalkulator Kredit",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontFamily = FontFamily.SansSerif
            )
            Text(
              text = "Simulasi cicilan real-time & transparan",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
              fontFamily = FontFamily.SansSerif
            )
          }
        }
      }

      // SECTION 1: DETIL BARANG DAN DP
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "1. Detail Barang & Uang Muka",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          // HARGA BARANG INPUT
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Harga Barang",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
              value = hargaBarangInput,
              onValueChange = { newValue ->
                val digits = newValue.toRawDigits().take(12)
                hargaBarangInput = if (digits.isNotEmpty()) formatNumericString(digits) else ""
                
                if (isDpAutoFilled) {
                  val priceVal = digits.toLongOrNull() ?: 0L
                  val autoFillDp = (priceVal * 0.20).toLong()
                  dpInput = if (priceVal > 0) formatNumericString(autoFillDp.toString()) else ""
                }
              },
              prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
              placeholder = { Text("0") },
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
              ),
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("harga_barang_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              ),
              trailingIcon = {
                if (hargaBarangInput.isNotEmpty()) {
                  IconButton(onClick = {
                    hargaBarangInput = ""
                    if (isDpAutoFilled) dpInput = ""
                  }) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Hapus Harga"
                    )
                  }
                }
              }
            )
          }

          // UANG MUKA (DP) INPUT
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Uang Muka (DP) - Min 20%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (!isDpValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
              )
              
              if (isPriceValid) {
                Text(
                  text = "$dpPercentage%",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (!isDpValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
              }
            }

            OutlinedTextField(
              value = dpInput,
              onValueChange = { newValue ->
                val digits = newValue.toRawDigits().take(12)
                dpInput = if (digits.isNotEmpty()) formatNumericString(digits) else ""
                isDpAutoFilled = digits.isEmpty()
              },
              prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
              placeholder = { Text("0") },
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
              ),
              keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
              ),
              singleLine = true,
              isError = !isDpValid,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dp_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (!isDpValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (!isDpValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
              ),
              trailingIcon = {
                if (dpInput.isNotEmpty()) {
                  IconButton(onClick = {
                    dpInput = ""
                    isDpAutoFilled = true
                  }) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Hapus DP"
                    )
                  }
                }
              }
            )

            // Real-Time DP Percentage Visual Progress Indicator
            if (isPriceValid) {
              Spacer(modifier = Modifier.height(2.dp))
              LinearProgressIndicator(
                progress = { (dpPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = if (!isDpValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
              )
            }

            // Real-time DP Validation Notice
            if (!isDpValid && isPriceValid) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = "Warning",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "DP minimal 20% (${formatRupiah(minDpRequired)})",
                  color = MaterialTheme.colorScheme.error,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            // PRESET BUTTONS FOR QUICK ACCESS
            if (isPriceValid) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                PresetButton(
                  label = "20% (Min)",
                  isSelected = dpPercentage == 20 || isDpAutoFilled,
                  onClick = {
                    val calculated = (rawHargaValue * 0.20).toLong()
                    dpInput = formatNumericString(calculated.toString())
                    isDpAutoFilled = true
                  },
                  modifier = Modifier.weight(1f)
                )
                PresetButton(
                  label = "30%",
                  isSelected = dpPercentage in 29..31 && !isDpAutoFilled,
                  onClick = {
                    val calculated = (rawHargaValue * 0.30).toLong()
                    dpInput = formatNumericString(calculated.toString())
                    isDpAutoFilled = false
                  },
                  modifier = Modifier.weight(1f)
                )
                PresetButton(
                  label = "40%",
                  isSelected = dpPercentage in 39..41 && !isDpAutoFilled,
                  onClick = {
                    val calculated = (rawHargaValue * 0.40).toLong()
                    dpInput = formatNumericString(calculated.toString())
                    isDpAutoFilled = false
                  },
                  modifier = Modifier.weight(1f)
                )
                PresetButton(
                  label = "50%",
                  isSelected = dpPercentage in 49..51 && !isDpAutoFilled,
                  onClick = {
                    val calculated = (rawHargaValue * 0.50).toLong()
                    dpInput = formatNumericString(calculated.toString())
                    isDpAutoFilled = false
                  },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }

      // SECTION 2: PILIH TENOR
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "2. Pilih Tenor Cicilan",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Pilih durasi pembayaran",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CreditTenor.values().forEach { tenor ->
              TenorOptionCard(
                tenor = tenor,
                isSelected = selectedTenor == tenor,
                onClick = { selectedTenor = tenor },
                modifier = Modifier
                  .weight(1f)
                  .testTag("tenor_select_${tenor.months}")
              )
            }
          }
        }
      }

      // SECTION 3: OPSI BIAYA ADMIN
      AnimatedVisibility(
        visible = isPriceValid,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { bayarAdmin = !bayarAdmin }
              .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (bayarAdmin) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (bayarAdmin) Icons.Default.CheckCircle else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (bayarAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(22.dp)
                )
              }
              Column {
                Text(
                  text = "Bayar Biaya Admin",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (bayarAdmin) "Biaya admin Rp 200.000 dimasukkan ke cicilan" else "Tanpa biaya admin, cicilan murni",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
              }
            }
            Switch(
              checked = bayarAdmin,
              onCheckedChange = { bayarAdmin = it },
              modifier = Modifier.testTag("admin_fee_switch")
            )
          }
        }
      }

      // SECTION 4: HASIL REAL-TIME SIMULASI
      AnimatedVisibility(
        visible = isPriceValid,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(24.dp)),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(24.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            
            if (!isDpValid) {
              Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = "Syarat DP minimum 20% tidak terpenuhi. Naikkan DP untuk simulasi yang valid.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
            ) {
              Text(
                text = "ESTIMASI CICILAN PER BULAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.25.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isDpValid) formatRupiah(cicilanPerBulan) else "-",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (isDpValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                modifier = Modifier.testTag("result_installment_text")
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Info",
                  tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                  modifier = Modifier.size(12.dp)
                )
                Text(
                  text = "Tenor Keanggotaan: ${selectedTenor.months} Bulan",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              BreakdownRow(
                label = "Harga Barang",
                value = formatRupiah(rawHargaValue),
                contentColor = MaterialTheme.colorScheme.onPrimary
              )
              BreakdownRow(
                label = "Uang Muka (DP)",
                value = "- ${formatRupiah(rawDpAmount)}",
                contentColor = MaterialTheme.colorScheme.onPrimary
              )
              BreakdownRow(
                label = "Pokok Utang Kredit",
                value = formatRupiah(pokokUtang),
                fontWeight = FontWeight.Bold,
                contentColor = MaterialTheme.colorScheme.onPrimary
              )
              BreakdownRow(
                label = "Total Kewajiban (${selectedTenor.months}x Cicilan)",
                value = formatRupiah(totalKewajiban),
                contentColor = MaterialTheme.colorScheme.onPrimary
              )
              BreakdownRow(
                label = "Biaya Administrasi (Tetap)",
                value = formatRupiah(biayaAdmin),
                contentColor = MaterialTheme.colorScheme.onPrimary
              )

            HorizontalDivider(
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
              modifier = Modifier.padding(vertical = 4.dp)
            )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Grand Total Harga",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "Total Kewajiban + DP + Admin",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isDpValid) formatRupiah(grandTotal) else "-",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = if (isDpValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }

      if (!isPriceValid) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Info",
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = "Masukkan nominal Harga Barang untuk memulai kalkulasi pembayaran.",
              textAlign = TextAlign.Center,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              modifier = Modifier.padding(horizontal = 8.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        Button(
          onClick = {
            hargaBarangInput = ""
            dpInput = ""
            isDpAutoFilled = true
            selectedTenor = CreditTenor.TENOR_12
            bayarAdmin = true
            focusManager.clearFocus()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("reset_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset Icon",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Reset Simulasi",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
fun PresetButton(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .defaultMinSize(minHeight = 36.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
      .border(
        width = 1.dp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 4.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun TenorOptionCard(
  tenor: CreditTenor,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val outlineColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
  
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(backgroundColor)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = outlineColor,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 10.dp, horizontal = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.DateRange,
        contentDescription = null,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = tenor.label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        softWrap = false
      )
    }
  }
}

@Composable
fun BreakdownRow(
  label: String,
  value: String,
  fontWeight: FontWeight = FontWeight.Normal,
  contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = contentColor.copy(alpha = 0.75f),
      modifier = Modifier.weight(1f),
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = fontWeight,
      color = contentColor,
      maxLines = 1,
      softWrap = false
    )
  }
}

@Preview(showBackground = true)
@Composable
fun CreditCalculatorScreenPreview() {
  MyApplicationTheme {
    CreditCalculatorScreen(modifier = Modifier.fillMaxSize())
  }
}
