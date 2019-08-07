/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ids.cas.web.helper

import java.awt.{Color, Font}
import java.awt.image.ImageFilter

import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator
import com.octo.captcha.component.image.color.RandomListColorGenerator
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage
import com.octo.captcha.component.word.FileDictionary
import com.octo.captcha.component.word.wordgenerator.ComposeDictionaryWordGenerator
import com.octo.captcha.engine.image.ListImageCaptchaEngine
import com.octo.captcha.image.gimpy.GimpyFactory

class GmailEngine extends ListImageCaptchaEngine {

  protected def buildInitialFactories(): Unit = {
    val minWordLength = Integer.valueOf(4)
    val maxWordLength = Integer.valueOf(5)
    val imageWidth = Integer.valueOf(90)
    val imageHeight = Integer.valueOf(35)
    val fontSize = 21

    val dictionnaryWords = new ComposeDictionaryWordGenerator(new FileDictionary("toddlist"))
    val randomPaster = new DecoratedRandomTextPaster(minWordLength, maxWordLength,
      new RandomListColorGenerator(Array(new Color(23, 170, 27), new Color(220, 34, 11), new Color(23, 67, 172))),
      Array.empty[TextDecorator])
    val background = new UniColorBackgroundGenerator(imageWidth, imageHeight, Color.white)
    val font = new RandomFontGenerator(Integer.valueOf(fontSize), Integer.valueOf(fontSize),
      Array(new Font("nyala", 1, fontSize), new Font("Bell MT", 0, fontSize),
        new Font("Credit valley", 1, fontSize)))

    val postDef = new ImageDeformationByFilters(Array.empty[ImageFilter])
    val backDef = new ImageDeformationByFilters(Array.empty[ImageFilter])
    val textDef = new ImageDeformationByFilters(Array.empty[ImageFilter])

    val word2image = new DeformedComposedWordToImage(font, background, randomPaster, backDef, textDef, postDef)

    addFactory(new GimpyFactory(dictionnaryWords, word2image))
  }
}
