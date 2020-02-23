package tech.lerk.sh.responses;

import sh.lrk.yahs.*;
import tech.lerk.sh.Main;

public class ColorCssResponse implements IResponse {
    @Override
    public Response getResponse(Request req) {
        int color = Main.getRandom().nextInt(0xffffff + 1);
        int complementary = color ^ 0x00ffffff;
        String colorString = String.format("#%06x", color);
        String complementaryString = String.format("#%06x", complementary);
        String css = ".r, .r:active, .r:focus { border-color: " + colorString + "!important; }" +
                ".r:focus { outline: " + colorString + "!important; }" +
                "button.r:focus, button.r:hover { background-color: " + colorString + "!important; color: " + complementaryString + "!important; };";
        return new Response(css.getBytes(), Status.OK, ContentType.TEXT_CSS);
    }
}
