/**
 * Get data for charts
 */

window.chartColors = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(201, 203, 207)'
};

var graphs = [];

function get_system_data(systemName, dataType, hours, callback) {
    jQuery.get("/webapi/timeline/" + systemName + "/" + dataType + "?hours=" + hours,
        {
            action: 'callback_id'
        }, callback);
}

function load_system_graph(canvasId, systemName, hours) {
    var can = jQuery(canvasId);
    var ctx = can.get(0).getContext("2d");

    var jumped_data = [];
    var destroyed_data = [];
    var docked_data = [];

    var labels = [];
    labels.push("now");
    for (var i = 1; i < hours; i++) {
        labels.push(i * -1 + " hrs");
    }

    get_system_data(systemName, "arrived", hours)

    var handle_destroyed = function (destroyed_resp) {
        destroyed_data = destroyed_resp.values;

        // now draw the graph!
        var config = {
            type: "line",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Destroyed",
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: destroyed_data,
                        lineTension: 0.8,
                        pointRadius: 2,
                        fill: true
                    },
                    {
                        label: "Docked",
                        backgroundColor: window.chartColors.yellow,
                        borderColor: window.chartColors.yellow,
                        data: docked_data,
                        lineTension: 0.5,
                        fill: false
                    },
                    {
                        label: "Jumped In",
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: jumped_data,
                        lineTension: 0.5,
                        fill: false
                    }

                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                layout: {
                    padding: 5
                },
                scales: {
                    yAxes: [{
                        stacked: true,
                        ticks: {
                            min: 0
                        }
                    }],
                    xAxes: [{
                        display: true,
                        ticks: {
                            min: -1 * hours,
                            max: 0
                        }
                    }]
                }
            }
        };

        graphs.push(new Chart(ctx, config));
    };

    get_system_data(systemName, "docked", hours, function (docked_resp) {
        docked_data = docked_resp.values;
        // got the docked data, now ask about jumps
        get_system_data(systemName, "jumpedin", hours, function (jumped_resp) {
            jumped_data = jumped_resp.values;
            // got the jump counts, request destroyed counts
            get_system_data(systemName, "destroyed", hours, handle_destroyed);
        })
    });

}