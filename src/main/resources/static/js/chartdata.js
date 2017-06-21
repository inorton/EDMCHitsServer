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
    //var container = can.parent().parent();
    //var $container = jQuery(container);
    //can.attr("width", $container.width() - 10);
    //can.attr("height", $container.height() - 3);

    var arrival_data = [];
    var destroyed_data = [];
    var labels = Array.apply(null, Array(hours)).map(function (_, i) {return i - hours;});

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
                        label: "Arrivals",
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: arrival_data,
                        lineTension: 0.1,
                        fill: true
                    },
                    {
                        label: "Destroyed",
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: destroyed_data,
                        lineTension: 0.1,
                        fill: true
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
                        display: false,
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

    var handle_arrived = function (arrived_resp) {
        arrival_data = arrived_resp.values;
        get_system_data(systemName, "destroyed", hours, handle_destroyed);
    };

    get_system_data(systemName, "arrived", hours, handle_arrived);

    /*
    jQuery.get("/webapi/timeline/" + systemName + "/arrived?hours=" + hours,
       {
           action: 'callback_id'
       },
       function (arrivals) {

       var labels = Array.apply(null, Array(hours)).map(function (_, i) {return i - hours;});
       if (arrivals) {

           var config = {
               type: "line",
               data: {
                   labels: labels,
                   datasets: [
                       {
                           label: "Arrivals",
                           backgroundColor: window.chartColors.green,
                           borderColor: window.chartColors.green,
                           data: arrivals.values,
                           lineTension: 0.1,
                           fill: true
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
                           display: false,
                           ticks: {
                               min: -1 * hours,
                               max: 0
                           }
                       }]
                   }
               }
           };

           graphs.push(new Chart(ctx, config));
       }
   });
   */
}