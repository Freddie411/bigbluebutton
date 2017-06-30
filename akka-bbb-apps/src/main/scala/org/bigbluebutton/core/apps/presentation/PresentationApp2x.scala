package org.bigbluebutton.core.apps.presentation

import akka.actor.ActorContext
import akka.event.Logging
import org.bigbluebutton.common2.domain.PresentationVO
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.running.LiveMeeting
import org.bigbluebutton.common2.domain.PageVO
import org.bigbluebutton.core.apps.Presentation

class PresentationApp2x(val liveMeeting: LiveMeeting,
  val outGW: OutMessageGateway)(implicit val context: ActorContext)
    extends EjectUserFromMeetingHdlr
    with NewPresentationMsgHdlr
    with SetCurrentPresentationPubMsgHdlr
    with GetPresentationInfoReqMsgHdlr
    with SetCurrentPagePubMsgHdlr
    with ResizeAndMovePagePubMsgHdlr
    with RemovePresentationPubMsgHdlr
    with PreuploadedPresentationsPubMsgHdlr
    with PresentationConversionUpdatePubMsgHdlr
    with PresentationPageCountErrorPubMsgHdlr
    with PresentationPageGeneratedPubMsgHdlr
    with PresentationConversionCompletedPubMsgHdlr {

  val log = Logging(context.system, getClass)

  def processPreuploadedPresentations(presentations: Vector[Presentation]) {
    presentations.foreach(presentation => {
      liveMeeting.presModel.addPresentation(presentation)
    })
  }

  def presentationConversionCompleted(presentation: Presentation) {
    liveMeeting.presModel.addPresentation(presentation)
  }

  def setCurrentPresentation(presentationId: String): Option[Presentation] = {
    liveMeeting.presModel.setCurrentPresentation(presentationId)
  }

  def getPresentationInfo(): Vector[Presentation] = {
    liveMeeting.presModel.getPresentations
  }

  def setCurrentPage(presentationId: String, pageId: String): Boolean = {
    liveMeeting.presModel.changeCurrentPage(presentationId, pageId)

    /* Need to figure out if this is still needed and if it is how to do it now
    Users.getCurrentPresenter(liveMeeting.users) foreach { pres =>
      handleStopPollRequest(StopPollRequest(props.meetingProp.intId, pres.id))
    }
    */
  }

  def resizeAndMovePage(presentationId: String, pageId: String,
    xOffset: Double, yOffset: Double, widthRatio: Double,
    heightRatio: Double): Option[PageVO] = {
    // Force coordinate that are out-of-bounds inside valid values
    // 0.25D is 400% zoom
    // 100D-checkedWidth is the maximum the page can be moved over
    val checkedWidth = Math.min(Math.max(widthRatio, 25D), 100D) //if (widthRatio <= 100D) widthRatio else 100D
    val checkedHeight = Math.min(Math.max(heightRatio, 25D), 100D)
    val checkedXOffset = Math.min(Math.max(xOffset, 0D), 100D - checkedWidth)
    val checkedYOffset = Math.min(Math.max(yOffset, 0D), 100D - checkedHeight)

    liveMeeting.presModel.resizePage(presentationId, pageId, checkedXOffset, checkedYOffset, checkedWidth, checkedHeight);
  }

  def removePresentation(presentationId: String): Option[Presentation] = {
    liveMeeting.presModel.removePresentation(presentationId)
  }
}