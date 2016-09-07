package net.mrkeks.clave.util

/** A structured way of writing down chains of probabilistic if-cases.
 *  
 *  (There must be a `markovElse`, for this to work.)*/

class MarkovIf[Return](rnd: Double, cond: Double, then: =>Return) {
  def markovElseIf(elseCond: Double)(elseThen: =>Return) = {
    if (rnd < cond) {
      this
    } else {
      new MarkovIf(rnd - cond, elseCond, elseThen)
    }
  }
  
  def markovElse(elseThen: =>Return) = {
    if (rnd < cond) {
      then
    } else {
      elseThen
    }
  }
}

object markovIf {
  def apply[Return](cond: Double)(then: =>Return) = {
    val rnd = Math.random()
    new MarkovIf(rnd, cond, then)
  }
}