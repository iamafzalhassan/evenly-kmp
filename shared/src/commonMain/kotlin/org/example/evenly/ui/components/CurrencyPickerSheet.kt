package org.example.evenly.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import evenly.shared.generated.resources.Res
import evenly.shared.generated.resources.currency_label
import evenly.shared.generated.resources.currency_name_aed
import evenly.shared.generated.resources.currency_name_aud
import evenly.shared.generated.resources.currency_name_chf
import evenly.shared.generated.resources.currency_name_czk
import evenly.shared.generated.resources.currency_name_dkk
import evenly.shared.generated.resources.currency_name_eur
import evenly.shared.generated.resources.currency_name_gbp
import evenly.shared.generated.resources.currency_name_inr
import evenly.shared.generated.resources.currency_name_jpy
import evenly.shared.generated.resources.currency_name_lkr
import evenly.shared.generated.resources.currency_name_nok
import evenly.shared.generated.resources.currency_name_pln
import evenly.shared.generated.resources.currency_name_sek
import evenly.shared.generated.resources.currency_name_usd
import org.example.evenly.model.Currency
import org.jetbrains.compose.resources.stringResource

@Composable
fun CurrencyPickerSheet(title: String, onDismiss: () -> Unit, onSelect: (Currency) -> Unit, selected: Currency?, modifier: Modifier = Modifier) {
    PickerSheet(modifier = modifier, icon = Icons.Outlined.CurrencyExchange, label = { it.name }, onDismiss = onDismiss, onSelect = onSelect, options = Currency.entries, selected = selected, subtitle = { currencyName(it) }, title = title)
}

@Composable
fun currencyLabel(currency: Currency): String = stringResource(Res.string.currency_label, currency.name, currencyName(currency))

@Composable
private fun currencyName(currency: Currency): String = stringResource(
    when (currency) {
        Currency.AED -> Res.string.currency_name_aed
        Currency.AUD -> Res.string.currency_name_aud
        Currency.CHF -> Res.string.currency_name_chf
        Currency.CZK -> Res.string.currency_name_czk
        Currency.DKK -> Res.string.currency_name_dkk
        Currency.EUR -> Res.string.currency_name_eur
        Currency.GBP -> Res.string.currency_name_gbp
        Currency.INR -> Res.string.currency_name_inr
        Currency.JPY -> Res.string.currency_name_jpy
        Currency.LKR -> Res.string.currency_name_lkr
        Currency.NOK -> Res.string.currency_name_nok
        Currency.PLN -> Res.string.currency_name_pln
        Currency.SEK -> Res.string.currency_name_sek
        Currency.USD -> Res.string.currency_name_usd
    },
)
