

function parseResponse(data) {
    var params = data.replaceAll("<br>","").replaceAll("\n","").split("&");
    var parsed = new Map();
    for(var i = 0; i < params.length; i++){
        var ind = params[i].indexOf("=");
        if(ind != -1){
            parsed.set(params[i].substr(0, ind), params[i].substr(ind + 1))
        }
    }
    return parsed
}

function printPlot(place, t, xpts, ypts){
    $.ajax({
        url: "/getplotdata",
        data: {
            vec: res_vec,
            x1: x1_fixed,
            x2: x2_fixed,
            t: t,
            omega: omega_fixed,
            xpts: xpts,
            ypts: ypts,
            m_z_size: m_z_size_fixed,
            m_g_size: m_g_size_fixed,
            m_zero: m_zero_fixed,
            m_gran: m_gran_fixed,
            u_z_size: u_z_size_fixed,
            u_g_size: u_g_size_fixed,
            u_zero: u_zero_fixed,
            u_gran: u_gran_fixed
        },
        type: 'get',
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log('status:' + XMLHttpRequest.status + ', status text: ' + XMLHttpRequest.statusText);
        },
        success: function (data) {
            var params = parseResponse(data);
            var plot_data = params.get("data");
            var lines = plot_data.split("#");
            arr = [];
            for(var i = 0; i < xpts; i++){
                var cur_line = lines[i].split(" ");
                tmp = [];
                for(var j = 0; j < cur_line.length; j++)
                    tmp.push(parseFloat(cur_line[j]));
                arr.push(tmp);
            }
            // console.log(arr);
            // console.log(data);
            console.log(params.get("data2data"));

            arr_x = [];
            var tmp = params.get("x_axis").split(" ");
            for(var i = 0; i < xpts; i++)
                arr_x.push(parseFloat(tmp[i]));

            arr_y = [];
            var tmp = params.get("y_axis").split(" ");
            for(var i = 0; i < ypts; i++)
                arr_y.push(parseFloat(tmp[i]));

            var trace1 = {
                z: arr,
                x: arr_x,
                y: arr_y,
                type: 'surface',
                opacity: 0.7
            };

            var trace2;
            if(params.has("data2data")){
                arr_x2 = [];
                var tmp = params.get("data2x").split(" ");
                for(var i = 0; i < tmp.length; i++)
                    arr_x2.push(parseFloat(tmp[i]));

                arr_y2 = [];
                var tmp = params.get("data2y").split(" ");
                for(var i = 0; i < tmp.length; i++)
                    arr_y2.push(parseFloat(tmp[i]));

                arr_f2 = [];
                var tmp = params.get("data2data").split(" ");
                for(var i = 0; i < tmp.length; i++)
                    arr_f2.push(parseFloat(tmp[i]));
                trace2 = {
                    z: arr_f2,
                    x: arr_x2,
                    y: arr_y2,
                    size: 12,
                    // type: 'scatter3d'
                    mode: 'markers',
                    // marker: {
                    //     size: 12,
                    //     line: {
                    //         color: 'rgba(217, 217, 217, 0.14)',
                    //         width: 0.5},
                    //     opacity: 0.8},
                    type: 'scatter3d'
                };
            }

            if(params.has("data2data"))
                data_pl = [trace1, trace2];
            // data_pl = [trace2];
            else
                data_pl = [trace1];
            var layout = {};

            Plotly.newPlot(place, data_pl, layout)
        }
    });
}

$(document).ready(() => {
    $('#submit_1').click(() => {
        var err_msg = "";
        if($('#sygma_top').val() === "")
            err_msg += "Необхідно заповнити чисельник сигма<br>\n";
        if($('#sygma_bottom').val() === "")
            err_msg += "Необхідно заповнити знаменник сигма<br>\n";
        if($('#ro_top').val() === "")
            err_msg += "Необхідно заповнити чисельник ро<br>\n";
        if($('#ro_bottom').val() === "")
            err_msg += "Необхідно заповнити знаменник ро<br>\n";
        if($('#beta_top').val() === "")
            err_msg += "Необхідно заповнити чисельник бета<br>\n";
        if($('#beta_bottom').val() === "")
            err_msg += "Необхідно заповнити знаменник бета<br>\n";

        if($('#x0').val() === "")
            err_msg += "Необхідно заповнити x(0)<br>\n";
        if($('#y0').val() === "")
            err_msg += "Необхідно заповнити y(0)<br>\n";
        if($('#z0').val() === "")
            err_msg += "Необхідно заповнити z(0)<br>\n";
        if($('#t_final').val() === "")
            err_msg += "Необхідно заповнити T<br>\n";
        if($('#n_pts').val() === "")
            err_msg += "Необхідно заповнити кількість точок<br>\n";

        if(parseFloat($('#sygma_bottom').val()) == 0)
            err_msg += "Знаменник сигма рівний нулю<br>\n";
        if(parseFloat($('#ro_bottom').val()) == 0)
            err_msg += "Знаменник ро рівний нулю<br>\n";
        if(parseFloat($('#beta_bottom').val()) == 0)
            err_msg += "Знаменник бета рівний нулю<br>\n";
        $('#error_msg').html(err_msg)

        if(err_msg === ""){
            $.ajax({
                url: "/calculate",
                data: {
                    sygma_top: $('#sygma_top').val(),
                    sygma_bottom: $('#sygma_bottom').val(),
                    ro_top: $('#ro_top').val(),
                    ro_bottom: $('#ro_bottom').val(),
                    beta_top: $('#beta_top').val(),
                    beta_bottom: $('#beta_bottom').val(),
                    x0: $('#x0').val(),
                    y0: $('#y0').val(),
                    z0: $('#z0').val(),
                    t_final: $('#t_final').val(),
                    n_pts: $('#n_pts').val()
                },
                type: 'get',
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    $('#error_msg').append('status:' + XMLHttpRequest.status + ', status text: ' + XMLHttpRequest.statusText);
                    alert('status:' + XMLHttpRequest.status + ', status text: ' + XMLHttpRequest.statusText);
                },
                success: function (data) {
                    $('#full_response').text(data);

                    var params = parseResponse(data);
                    arr_z = [];
                    var tmp = params.get("attr_z").split(" ");
                    for(var i = 0; i < tmp.length; i++)
                        arr_z.push(parseFloat(tmp[i]));

                    arr_x = [];
                    var tmp = params.get("attr_x").split(" ");
                    for(var i = 0; i < tmp.length; i++)
                        arr_x.push(parseFloat(tmp[i]));

                    arr_y = [];
                    var tmp = params.get("attr_y").split(" ");
                    for(var i = 0; i < tmp.length; i++)
                        arr_y.push(parseFloat(tmp[i]));

                    var trace1 = {
                        z: arr_z,
                        x: arr_x,
                        y: arr_y,
                        opacity: 0.7,
                        size: 1,
                        mode: 'lines',
                        type: 'scatter3d'
                    };
                    data_pl = [trace1];
                    var layout = {
                        height: 600,
                        width: 700,
                        title: {
                            text: 'Атрактор Лоренца'
                        }
                        // scene: {
                        //     xaxis:{title: 'x axis'}
                        // }
                    };
                    Plotly.newPlot("attr_plot", data_pl, layout)

                    sens_u1 = [];
                    var tmp = params.get("sens_u1").split(" ");
                    for(var i = 0; i < tmp.length; i++)
                        sens_u1.push(parseFloat(tmp[i]));

                    sens_u2 = [];
                    var tmp = params.get("sens_u2").split(" ");
                    for(var i = 0; i < tmp.length; i++)
                        sens_u2.push(parseFloat(tmp[i]));

                    sens_u3 = [];
                    var tmp = params.get("sens_u3").split(" ");
                    for(var i = 0; i < tmp.length; i++)
                        sens_u3.push(parseFloat(tmp[i]));

                    var trace2 = {
                        z: sens_u3,
                        x: sens_u1,
                        y: sens_u2,
                        opacity: 0.7,
                        size: 1,
                        mode: 'lines',
                        type: 'scatter3d'
                    };
                    data_pl_2 = [trace2];
                    var layout_2 = {
                        height: 600,
                        width: 700,
                        title: {
                            text: 'Матриця чутливості'
                        },
                        scene: {
                            xaxis:{title: 'u1'},
                            yaxis:{title: 'u2'},
                            zaxis:{title: 'u3'}
                        }
                    };
                    Plotly.newPlot("sens_plot", data_pl_2, layout_2)
                }
            })
        }

    })
});




