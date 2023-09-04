package ink.ziip.hammer.battlebox.api.object;

import ink.ziip.hammer.battlebox.api.object.area.Area;
import ink.ziip.hammer.battlebox.api.object.team.TeamCard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueTeam {

    private TeamCard teamCard1, teamCard2;
    private Area area;
}
