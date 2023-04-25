package core.kite

import core.kite.KiteAuthenticator.getKiteConnect
import com.zerodhatech.models.OrderParams
import com.zerodhatech.ticker.KiteTicker
import kotlin.random.Random

object Providers {

    object MarginProvider {
        fun getMargin(): String {
            val kiteSdk = getKiteConnect()
            val margins = kiteSdk.getMargins("equity")
            return margins.available.cash
        }
    }

    object OrderProvider {
        fun placeOrder(orderParams: OrderParams, variety: String): String {
            val kiteSdk = getKiteConnect()
            val order = kiteSdk.placeOrder(orderParams, variety)
            return order.orderId
        }

        fun createOrder(
            exchange: String = "NSE",
            tradingSymbol: String,
            transactionType: String,
            quantity: Int,
            price: Double,
            product: String = "MIS",
            orderType: String = "MARKET",
            validity: String = "DAY",
            disclosedQuantity: Int?,
            triggerPrice: Double?,
            squareOffValue: Double?,
            stopLossValue: Double?,
            trailingStopLoss: Double?,
            tag: String = "autotrader order ${Random(1000000)}"
        ): OrderParams {
            val orderParams = OrderParams()
            orderParams.exchange = exchange
            orderParams.tradingsymbol = tradingSymbol
            orderParams.transactionType = transactionType
            orderParams.quantity = quantity
            orderParams.price = price
            orderParams.product = product
            orderParams.orderType = orderType
            orderParams.validity = validity
            orderParams.disclosedQuantity = disclosedQuantity
            orderParams.triggerPrice = triggerPrice
            orderParams.squareoff = squareOffValue
            orderParams.stoploss = stopLossValue
            orderParams.trailingStoploss = trailingStopLoss
            orderParams.tag = tag

            return orderParams
        }
    }

    object TickerLiveDataProvider {

        private lateinit var tickerProvider: KiteTicker

        fun subscribe(tradingSymbol: Long, kiteListener: KiteListener) {
            val kiteSdk = getKiteConnect()
            tickerProvider = KiteTicker(kiteSdk.accessToken, kiteSdk.apiKey)
            tickerProvider.setOnConnectedListener {
                kiteListener.onTickerConnected()
                tickerProvider.subscribe(arrayListOf(tradingSymbol))
                tickerProvider.setMode(arrayListOf(tradingSymbol), KiteTicker.modeQuote)
            }

            tickerProvider.setOnDisconnectedListener {
                kiteListener.onTickerDisconnected()
            }

            tickerProvider.setOnOrderUpdateListener {
                kiteListener.onOrderUpdate(it)
            }

            tickerProvider.setOnTickerArrivalListener {
                kiteListener.onTickerArrival(it)
            }

            tickerProvider.setTryReconnection(true)
            tickerProvider.setMaximumRetries(10)
            tickerProvider.setMaximumRetryInterval(30)
            tickerProvider.connect()
        }

        fun unsubscribe(tradingSymbol: Long) {
            tickerProvider.unsubscribe(arrayListOf(tradingSymbol))
            tickerProvider.disconnect()
        }

    }

    object TokenProvider {

        fun getInstrumentToken(tradingSymbol: String): Long {
            val kiteSdk = getKiteConnect()
            val instruments = kiteSdk.instruments
            return instruments.find { it.tradingsymbol == tradingSymbol }?.instrument_token ?: 0
        }

    }


}