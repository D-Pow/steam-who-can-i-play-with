package org.whocaniplaywith.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "game_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDetail {
    @Id
    @Column(name = "steam_app_id")
    private String steamAppid;

    @Column(name = "game_detail_json")
    private String gameDetailJson;
}
