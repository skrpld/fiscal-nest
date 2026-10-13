# Project a multi-period forecast

Use `FORECAST` mode to turn dated, recurring income and expenses into a plan, a cash view and
daily budget figures for several periods ahead.

Prerequisites: the engine is on the classpath (see [Install](../../README.md#install)) and you have an
`EngineConfig` (the one from [Usage](../../README.md#usage) is used below).

1. Describe income and expenses as events. Each event has an `id` for client-side correlation, an
   amount per occurrence, a recurrence and a date range (`endDate = null` means unbounded):

   ```kotlin
   val salary = IncomeEvent(
       id = "salary",
       amount = BigDecimal("50000"),
       recurrence = EventRecurrence.EveryNMonths(n = 1, dayOfMonth = 1),
       startDate = LocalDate.of(2026, 1, 1),
       endDate = null
   )
   val rent = ExpenseEvent(
       id = "rent",
       amount = BigDecimal("20000"),
       isMandatory = true,
       recurrence = EventRecurrence.EveryNMonths(n = 1, dayOfMonth = 5),
       startDate = LocalDate.of(2026, 1, 5),
       endDate = null
   )
   ```

   Recurrence patterns: `OneTime`, `EveryNDays(n)`, `EveryNMonths(n, dayOfMonth)` (`31` = end of
   month). Details: [API reference](../reference/api.md#31-eventrecurrence-sealed-class).

2. Call `BudgetCalculator.calculateForecast` with the first period, today's date, unscheduled
   spending so far and the number of periods. A first period that spans whole months makes all
   later periods follow calendar months:

   ```kotlin
   val forecast = BudgetCalculator.calculateForecast(
       ForecastInput(
           incomeEvents = listOf(salary),
           expenseEvents = listOf(rent),
           periodStart = LocalDate.of(2026, 8, 1),
           periodEnd = LocalDate.of(2026, 8, 31),
           currentDate = LocalDate.of(2026, 8, 7),
           alreadySpent = BigDecimal("3500"),
           forecastPeriods = 3,
           config = config,
           cushionState = CushionState(current = BigDecimal("5000"), target = BigDecimal("20000"))
       )
   )
   ```

   Pass only spending that no expense event covers as `alreadySpent`.

3. Read one `ForecastResult` per period. With the input above:

   ```kotlin
   val august = forecast[0]
   august.distribution.freeRemainder  // 21000.00  plan: 50000 - 20000 - 4000 cushion - 5000 piggy
   august.cashFlow.paidMandatory      // 20000.00  rent of Aug 5 is already paid
   august.cashFlow.mustReserve        // 4000.00   cushion top-up, as configured
   august.cashFlow.available          // 22500.00  50000 - 20000 - 3500 already spent - 4000
   august.closingBalance              // 17500.00  21000 - 3500, carried into September
   august.dailyMetrics.dailyActual    // 700.00    17500 / 25 remaining days
   august.dailyMetrics.dailyCashflow  // 900.00    22500 / 25

   val september = forecast[1]
   september.periodStart              // 2026-09-01
   september.periodEnd                // 2026-09-30
   september.openingBalance           // 17500.00
   september.distribution.totalIncome // 50000.00  carried money is not redistributed
   september.freeBalance              // 39500.00  17500 carried + 22000 new free remainder
   ```

   Show `cashFlow.available` or `dailyMetrics.dailyCashflow` as "safe to spend now", and
   `distribution` as the period plan.

4. If the input comes from untrusted users, bound `forecastPeriods` and the number of events: the
   cost grows linearly with both. Invalid input throws `IllegalArgumentException`
   ([exception reference](../reference/api.md#6-exception-reference)).

Related: [API reference](../reference/api.md), [Architecture: plan and cash](../explanation/architecture.md#64-plan-and-cash-two-views-of-one-period).
