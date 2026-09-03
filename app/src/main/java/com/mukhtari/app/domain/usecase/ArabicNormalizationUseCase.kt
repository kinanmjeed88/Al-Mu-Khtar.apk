package com.mukhtari.app.domain.usecase

class ArabicNormalizationUseCase {
    operator fun invoke(input: String): String {
        var text = input
        
        // Remove diacritics (tashkeel)
        val diacriticsRegex = Regex("[\u064B-\u065F]")
        text = text.replace(diacriticsRegex, "")
        
        // Remove tatweel (kashida)
        val tatweelRegex = Regex("\u0640")
        text = text.replace(tatweelRegex, "")
        
        // Unify alef variants
        val alefVariantsRegex = Regex("[\u0622\u0623\u0625\u0671]")
        text = text.replace(alefVariantsRegex, "\u0627")
        
        // Unify taa marbouta to haa
        val taaMarboutaRegex = Regex("\u0629")
        text = text.replace(taaMarboutaRegex, "\u0647")
        
        // Unify yaa variants (alef maksoura -> yaa)
        val alefMaksouraRegex = Regex("\u0649")
        text = text.replace(alefMaksouraRegex, "\u064A")
        
        // Remove extra spaces
        text = text.replace(Regex("\\s+"), " ").trim()
        
        return text
    }
}
