package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro

/**
 * Distributes [amount] in proportion to [weights] (§ 1606 Abs. 3 BGB).
 *
 * - The shares add up to exactly [amount]; cumulative rounding loses no cent.
 * - No share exceeds its weight (ability to pay). If the weights do not suffice, every share equals its weight (Mangelfall).
 */
internal fun distribute(amount: Euro, weights: List<Euro>): List<Euro> {
    val total = weights.fold(Euro.ZERO, Euro::plus)
    if (total == Euro.ZERO) return weights.map { Euro.ZERO }
    if (total <= amount) return weights

    var cumulativeWeight = Euro.ZERO
    var distributedSoFar = Euro.ZERO
    return weights.map { weight ->
        cumulativeWeight += weight
        val distributedUpToHere = amount.proportion(cumulativeWeight, total)
        (distributedUpToHere - distributedSoFar).also { distributedSoFar = distributedUpToHere }
    }
}
