/*
 * Copyright 2022 University of Basel, Graphics and Vision Research Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package apps.registration

import api.ModelFittingParameters
import api.helper.{LogHelper, PosteriorHelper}
import api.sampling.loggers.JSONStateLogger
import apps.DemoDatasetLoader
import scalismo.ui.api.ScalismoUI
import scalismo.utils.Random.implicits.randomGenerator

import java.awt.Color
import java.io.File

object DemoPosteriorVisualizationFemur extends App {
  scalismo.initialize()

  val burnInPhase = 100

  val (model, _) = DemoDatasetLoader.femur.modelGauss()
  val (target, _) = DemoDatasetLoader.femur.target()

  val jsonFile = new File("data/femur/targetFittingICP.json")
  val fullLog = JSONStateLogger.loadLog(jsonFile)

  val logSamples = LogHelper.samplesFromLog(fullLog, takeEveryN = 50, total = 10000, burnInPhase)
  println(s"Number of samples from log: ${logSamples.length}/${fullLog.length - burnInPhase}")
  val logShapes = LogHelper.logSamples2shapes(model, logSamples.map(_._1))

  val bestLogInstance = JSONStateLogger.getBestStateFromLog(fullLog)
  val bestModelParameters = JSONStateLogger.jsonFormatToModelFittingParameters(bestLogInstance)
  val best = ModelFittingParameters.modelInstanceShapePoseScale(model, bestModelParameters)

  val colorMap_normalVariance = PosteriorHelper.computeDistanceMapFromMeshesNormal(logShapes, best)
  val colorMap_posteriorEstimate = PosteriorHelper.computeDistanceMapFromMeshesTotal(logShapes, best)

  val ui = ScalismoUI(s"Posterior visualization")
  val modelGroup = ui.createGroup("model")
  val targetGroup = ui.createGroup("target")
  val colorGroup = ui.createGroup("color")
  val showModel = ui.show(modelGroup, model, "model")
  showModel.referenceView.opacity = 0.0
  val showTarget = ui.show(targetGroup, target, "target")
  showTarget.color = Color.YELLOW
  showTarget.opacity = 0.0
  ui.show(colorGroup, colorMap_posteriorEstimate, "posterior")
  ui.show(colorGroup, colorMap_normalVariance, "normal")
}
