$(function () {
    load();
    initModal();
});

function create(name, url) {
        $.post("/service", JSON.stringify({name: name, url: url}), function () {
            load();
        }, "json");
    }

function remove(id) {
    $.ajax({
        method: "DELETE",
        url: "/service/" + id
    }).done(function () {
        load();
    });
}

function update(id, name, url) {
    $.ajax({
        method: "PUT",
        url: "/service/" + id,
        data: JSON.stringify({name: name, url: url, id: id})
    }).done(function () {
        load();
    });
}

function load() {
    $("#content").children().remove();
    $.getJSON("/services", function (data) {
        $.each(data, function (key, val) {
            $("<tr><td style='display: none' >" + val.id + "</td><td>" + val.name + "</td><td>" + val.url + "</td>" +"</td><td>" + val.status + "</td>" +
                    "<td>" +
                    "<button data-action='edit' class='btn btn-primary btn-sm service-edit' " +
                    "data-toggle='modal' " +
                    "data-target='#serviceModal' " +
                    "data-name='" + val.name + "' " +
                    "data-url='" + val.url + "' " +
                    "data-status='" + val.status + "' " +
                    "data-id='" + val.id + "'>" +
                    "<span class='glyphicon glyphicon-pencil'></span>" +
                    "</button>" +
                    "&nbsp;" +
                    "<button class='btn btn-danger btn-sm service-delete' data-id='" + val.id + "'>" +
                    "   <span class='glyphicon glyphicon-minus'></span>" +
                    "</button>" +
                    "</td>" +
                    "</tr>").appendTo("#content");
        });
        initCallbacks();
    });
}

function initCallbacks() {
    $(".service-delete").unbind().click(function() {
       var id = $(this).data("id");
       remove(id);
    });
}

function pageloadEvery(t) {
  setTimeout('location.reload(true)', t);
}

function initModal() {
    $("#serviceModal").on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var action = button.data('action');
        var id = button.data('id');
        var serviceAction = $("#serviceAction");
        serviceAction.unbind();
        var modal = $(this);
        if (action === "add") {
            modal.find('.modal-title').text("Add a service");
            modal.find('#service-name').val("");
            modal.find('#service-url').val("");
            serviceAction.click(function () {
                create($("#service-name").val(), $("#service-url").val());
                $('#serviceModal').modal('toggle');
            });
        } else {
            modal.find('.modal-title').text("Edit a service");
            modal.find('#service-name').val(button.data("name"));
            modal.find('#service-url').val(button.data("url"));
            serviceAction.click(function () {
                update(id, $("#service-name").val(), $("#service-url").val());
                $('#serviceModal').modal('toggle');
            });
        }
    })
}