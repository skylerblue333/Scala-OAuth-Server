package com.skycoin4444.oauth

object Main:
  def main(args: Array[String]): Unit =
    val verifier = "a" * 43
    val lab = OAuthLab(Map("demo-client" -> Set("https://localhost.example/callback")))
    val code = lab.authorize("demo-client", "https://localhost.example/callback", "demo-user", OAuthLab.s256(verifier))
    val token = lab.exchange(code.value, "demo-client", "https://localhost.example/callback", verifier)
    println(s"{\"service\":\"sky-scala-oauth\",\"status\":\"ready\",\"subject\":\"${token.subject}\"}")
