package net.mrkeks.clave.util

/** A structured way of writing down chains of probabilistic if-cases.
 *  
 *  (There must be a `markovElse`, for this to work.)*/

class markovIf[Return](rnd: Double, cond: Double, thenc: =>Return) {
  def markovElseIf(elseCond: Double)(elseThen: =>Return) = {
    if (rnd < cond) {
      this
    } else {
      new markovIf(rnd - cond, elseCond, elseThen)
    }
  }
  
  def markovElse(elseThen: =>Return) = {
    if (rnd < cond) {
      thenc
    } else {
      elseThen
    }
  }
}

object markovIf {
  def apply[Return](cond: Double)(thenc: =>Return) = {
    val rnd = Math.random()
    new markovIf(rnd, cond, thenc)
  }
}