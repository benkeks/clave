package net.mrkeks.clave.view

import org.scalajs.dom

class GUI {
  
  private val hudContainer = dom.document.createElement("div")
  hudContainer.classList.add("hud")
  private val scoreText = dom.document.createTextNode("Score: 0") 
  hudContainer.appendChild(scoreText)
  
  dom.document.body.appendChild(hudContainer)
  
  def setScore(score: Int) {
    scoreText.textContent = "Score: "+score
  }
}